package com.hjy.bluetooth.operator.impl

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import com.hjy.bluetooth.HBluetooth
import com.hjy.bluetooth.HBluetooth.BleConfig
import com.hjy.bluetooth.entity.BluetoothDevice
import com.hjy.bluetooth.exception.BluetoothException
import com.hjy.bluetooth.inter.ConnectCallBack
import com.hjy.bluetooth.inter.SendCallBack
import com.hjy.bluetooth.operator.abstra.Sender
import com.hjy.bluetooth.utils.ArrayUtils.splitBytes
import com.hjy.bluetooth.utils.BleNotifier.closeNotification
import com.hjy.bluetooth.utils.LockStore
import com.hjy.bluetooth.utils.ReceiveHolder.receiveClassicBluetoothReturnData
import java.io.DataInputStream
import java.io.IOException
import java.util.*

/**
 * Created by _H_JY on 2018/10/22.
 */
class BluetoothSender : Sender() {
    private val handler = Handler(Looper.getMainLooper())
    private var socket: BluetoothSocket? = null
    private var gatt: BluetoothGatt? = null
    private var characteristic: BluetoothGattCharacteristic? = null
    private var connector: BluetoothConnector? = null
    private var connectCallBack: ConnectCallBack? = null
    private var sendCallBack: SendCallBack? = null
    private var mBleConfig: BleConfig? = null
    var type = 0

    fun setConnector(connector: BluetoothConnector): BluetoothSender = apply {
        this.connector = connector
    }

    override fun discoverServices() {
        gatt?.discoverServices()
    }

    override fun <G> initSenderHelper(g: G): G? {
        return if (g is BluetoothGattCharacteristic) {
            ((g as BluetoothGattCharacteristic?).also { characteristic = it }) as G
        } else null
    }

    @Synchronized
    override fun destroyChannel() {
        connector?.cancelConnectAsyncTask()

        socket?.let { sIt ->
            try {
                sIt.close()
                socket = null
                connectCallBack?.onDisConnected()
                connectCallBack = null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        //Ble disconnect
        gatt?.let {
            //Close ble notification
            closeNotification()
            //Will go to onConnectionStateChange()，and call gatt.close() to release
            it.disconnect()
            refreshGattCache()
        }
    }

    @Synchronized
    private fun refreshGattCache() {
        try {
            val method = BluetoothGatt::class.java.getMethod("refresh")
            if (method != null && gatt != null) {
                method.invoke(gatt)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun resetCallBack() {
        sendCallBack = null
    }

    private fun sendFailCallBack(failMsg: String) {
        sendCallBack?.let {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                it.onSendFailure(BluetoothException(failMsg))
            } else {
                //Call back on UI thread
                handler.post { it.onSendFailure(BluetoothException(failMsg)) }
            }
        }
    }

    private fun sendingCallBack(command: ByteArray) {
        sendCallBack?.let {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                it.onSending(command)
            } else {
                //Call back on UI thread
                handler.post { it.onSending(command) }
            }
        }
    }

    override fun send(command: ByteArray, sendCallBack: SendCallBack?) {
        this.sendCallBack = sendCallBack
        if (LockStore.getLock(LOCK_NAME)) {
            if (type == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                socket?.let {
                    Thread(Runnable {
                        try {
                            //Clean input stream before write.
                            val `is` = it.inputStream
                            var r = 1
                            while (r > 0) {
                                r = `is`.available()
                                if (r > 0) {
                                    val b = ByteArray(r)
                                    `is`.read(b, 0, r)
                                }
                            }
                            Thread.sleep(50)
                            sendingCallBack(command)

                            //Send command.
                            val os = it.outputStream
                            os.write(command)
                            os.flush()

                            //Get return's data.
                            val dis = DataInputStream(it.inputStream)
                            val buffer = ByteArray(1024)
                            val size = it.inputStream.read(buffer)
                            val result = ByteArray(size)
                            System.arraycopy(buffer, 0, result, 0, size)
                            receiveClassicBluetoothReturnData(dis, result)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            sendFailCallBack("Bluetooth socket write IOException")
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                            sendFailCallBack("Bluetooth socket write InterruptedException")
                        } finally {
                            LockStore.releaseLock(LOCK_NAME)
                        }
                    }).start()
                } ?: let {
                    LockStore.releaseLock(LOCK_NAME)
                }

            } else if (gatt != null && characteristic != null && type == BluetoothDevice.DEVICE_TYPE_LE) {

                //When the packet length exceeds 20, it needs to be sent by subcontracting
                if (mBleConfig == null) {
                    mBleConfig = HBluetooth.getInstance().bleConfig
                }
                var splitPacketToSendWhenCmdLenBeyond = false
                var sendTimeInterval = 0
                var splitLen = 20
                var serviceUUID: String? = null
                var writeUUID: String? = null
                mBleConfig?.let {
                    splitPacketToSendWhenCmdLenBeyond = it.splitPacketToSendWhenCmdLenBeyond
                    sendTimeInterval = it.sendTimeInterval
                    splitLen = it.eachSplitPacketLen
                    serviceUUID = it.serviceUUID
                    writeUUID = it.writeCharacteristicUUID
                }

                if (splitPacketToSendWhenCmdLenBeyond && command.size > splitLen) {
                    //Split packet to send
                    val objects = splitBytes(command, splitLen)
                    for (`object` in objects) {
                        val onceCmd = `object` as ByteArray
                        bleSendCommand(onceCmd, serviceUUID!!, writeUUID!!)
                        try {
                            Thread.sleep(sendTimeInterval.toLong())
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    //If not set splitPacketWhenCmdLenBeyond=true on BleConfig,you need to set mtu when you want to send commands longer than 20
                    bleSendCommand(command, serviceUUID!!, writeUUID!!)
                }
                LockStore.releaseLock(LOCK_NAME)
            } else {
                LockStore.releaseLock(LOCK_NAME)
            }
        }
    }


    /**
     * Ble send command
     *
     * @param command
     * @param serviceUUID
     * @param writeUUID
     * @param sendCallBack
     */
    private fun bleSendCommand(command: ByteArray, serviceUUID: String, writeUUID: String) {
        //Instead, get the characteristic before sending the command every time
        val service: BluetoothGattService = gatt!!.getService(UUID.fromString(serviceUUID))
        if (service != null) {
            characteristic = service.getCharacteristic(UUID.fromString(writeUUID))

            if (characteristic == null) {
                sendFailCallBack("The WriteCharacteristic is null, please check your writeUUID whether right")
                return
            }

            //Check whether can write

            //Check whether can write
            if (characteristic!!.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) {
                sendFailCallBack("This characteristic not support write")
                return
            }
            characteristic!!.value = command
            sendingCallBack(command)
            if (!gatt!!.writeCharacteristic(characteristic)) {
                sendFailCallBack("Gatt writeCharacteristic fail, please check command or change the value of sendTimeInterval if you have set it")
            }
        } else {
            sendFailCallBack("Main bluetoothGattService is null,please check the serviceUUID whether right")
        }
    }


    override fun <T> initChannel(o: T, type: Int, connectCallBack: ConnectCallBack?): T? {
        this.connectCallBack = connectCallBack
        if (o is BluetoothSocket) {
            this.type = BluetoothDevice.DEVICE_TYPE_CLASSIC
            return ((o as BluetoothSocket?).also { socket = it }) as T
        } else if (o is BluetoothGatt) {
            this.type = BluetoothDevice.DEVICE_TYPE_LE
            return ((o as BluetoothGatt?).also { gatt = it }) as T
        }
        return null
    }


    override fun readCharacteristic(serviceUUID: String?, characteristicUUID: String?, sendCallBack: SendCallBack?) {
        this.sendCallBack = sendCallBack
        gatt?.let {
            val service: BluetoothGattService = it.getService(UUID.fromString(serviceUUID))
            if (service != null) {
                val characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID))
                if (characteristic == null) {
                    sendFailCallBack("This Characteristic is null, please check the characteristicUUID whether right")
                    return
                }

                //Check whether can read
                if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ == 0) {
                    sendFailCallBack("This characteristic not support read")
                    return
                }
                if (!it.readCharacteristic(characteristic)) {
                    sendFailCallBack("Gatt readCharacteristic fail")
                } else {
                }
            } else sendFailCallBack("BluetoothGattService is null,please check the serviceUUID whether right")
        } ?: sendFailCallBack("BluetoothGatt is null")
    }

    companion object {
        private const val LOCK_NAME = "SendCmdLock"
    }
}