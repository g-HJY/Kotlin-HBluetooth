package com.hjy.bluetooth.operator.impl

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothSocket
import com.hjy.bluetooth.entity.BluetoothDevice
import com.hjy.bluetooth.inter.ConnectCallBack
import com.hjy.bluetooth.inter.SendCallBack
import com.hjy.bluetooth.operator.abstra.Sender
import com.hjy.bluetooth.utils.LockStore
import java.io.DataInputStream
import java.io.IOException

/**
 * Created by _H_JY on 2018/10/22.
 */
class BluetoothSender : Sender() {
    private var socket: BluetoothSocket? = null
    private var gatt: BluetoothGatt? = null
    private var characteristic: BluetoothGattCharacteristic? = null
    private var connector: BluetoothConnector? = null
    private var connectCallBack: ConnectCallBack? = null
    var type = 0

    fun setConnector(connector: BluetoothConnector): BluetoothSender = apply {
        this.connector = connector
    }

    override fun discoverServices() {
        gatt?.let { it.discoverServices() }
    }

    override fun <G> initSenderHelper(g: G): G? {
        return if (g is BluetoothGattCharacteristic) {
            ((g as BluetoothGattCharacteristic?).also { characteristic = it }) as G
        } else null
    }

    @Synchronized
    override fun destroyChannel() {
        connector?.let {
            it.cancelConnectAsyncTask()
        }
        socket?.let { sIt ->
            try {
                sIt.close()
                socket = null
                connectCallBack?.let { it.onDisConnected() }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        gatt?.let {
            //will go to onConnectionStateChange()，and call gatt.close() to release
            it.disconnect()
        }
    }

    override fun send(command: ByteArray?, sendCallBack: SendCallBack?) {
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
                            sendCallBack?.onSending()

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
                            sendCallBack?.onReceived(dis, result)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } finally {
                            LockStore.releaseLock(LOCK_NAME)
                        }
                    }).start()
                } ?: let {
                    LockStore.releaseLock(LOCK_NAME)
                }

            } else if (gatt != null && characteristic != null && type == BluetoothDevice.DEVICE_TYPE_LE) {

                connector?.setSendCallBack(sendCallBack)

                characteristic!!.value = command
                sendCallBack?.onSending()
                gatt!!.writeCharacteristic(characteristic)
                LockStore.releaseLock(LOCK_NAME)
            } else {
                LockStore.releaseLock(LOCK_NAME)
            }
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

    companion object {
        private const val LOCK_NAME = "SendCmdLock"
    }
}