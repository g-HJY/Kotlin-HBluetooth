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
    var socket: BluetoothSocket? = null
    var gatt: BluetoothGatt? = null
    var characteristic: BluetoothGattCharacteristic? = null
    private var connector: BluetoothConnector? = null
    private var connectCallBack: ConnectCallBack? = null
    var type = 0

    fun setConnector(connector: BluetoothConnector?): BluetoothSender {
        this.connector = connector
        return this
    }

    override fun discoverServices() {
        if (gatt != null) {
            gatt!!.discoverServices()
        }
    }

    override fun <G> initSenderHelper(g: G): G? {
        return if (g is BluetoothGattCharacteristic) {
            ((g as BluetoothGattCharacteristic?).also { characteristic = it }) as G
        } else null
    }

    @Synchronized
    override fun destroyChannel() {
        if (connector != null) {
            connector!!.cancelConnectAsyncTask()
        }
        if (socket != null) {
            try {
                socket!!.close()
                socket = null
                if (connectCallBack != null) {
                    connectCallBack!!.onDisConnected()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (gatt != null) {
            //will go to onConnectionStateChange()，and call gatt.close() to release
            gatt!!.disconnect()
        }
    }

    override fun send(command: ByteArray?, sendCallBack: SendCallBack?) {
        if (LockStore.getLock(LOCK_NAME)) {
            if (socket != null && type == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                Thread(Runnable {
                    try {
                        //Clean input stream before write.
                        val `is` = socket!!.inputStream
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
                        val os = socket!!.outputStream
                        os.write(command)
                        os.flush()

                        //Get return's data.
                        val dis = DataInputStream(socket!!.inputStream)
                        val buffer = ByteArray(1024)
                        val size = socket!!.inputStream.read(buffer)
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
            } else if (gatt != null && characteristic != null && type == BluetoothDevice.DEVICE_TYPE_LE) {
                if (connector != null && sendCallBack != null) {
                    connector!!.setSendCallBack(sendCallBack)
                }
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