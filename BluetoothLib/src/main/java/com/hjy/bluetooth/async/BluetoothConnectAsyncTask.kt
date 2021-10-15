package com.hjy.bluetooth.async

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.hjy.bluetooth.HBluetooth
import com.hjy.bluetooth.constant.BluetoothState
import com.hjy.bluetooth.inter.ConnectCallBack
import com.hjy.bluetooth.operator.abstra.Sender
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Created by _H_JY on 2018/10/22.
 * Connected thread for classic bluetooth.
 */
class BluetoothConnectAsyncTask(private val mContext: Context, private val bluetoothDevice: BluetoothDevice, private val connectCallBack: ConnectCallBack?) : WeakAsyncTask<Void?, Void?, Int, Context?>(mContext) {
    private lateinit var bluetoothSocket: BluetoothSocket
    private var sender: Sender? = null
    private var handler: Handler? = null

    override fun doInBackground(target: Context?, vararg params: Void?): Int {
        if (bluetoothSocket != null) {
            if (handler == null) {
                handler = Handler(Looper.getMainLooper())
            }
            handler!!.post { connectCallBack?.onConnecting() }
            val maxTries = 3
            for (i in 0 until maxTries) {
                try {
                    bluetoothSocket.connect()
                    break
                } catch (e: IOException) {
                    e.printStackTrace()
                    try {
                        bluetoothSocket.close()
                    } catch (e1: IOException) {
                        e1.printStackTrace()
                    }
                }
            }
            val hBluetooth = HBluetooth.getInstance(mContext)
            hBluetooth.isConnected = bluetoothSocket.isConnected
            if (bluetoothSocket.isConnected) {
                sender = hBluetooth.sender()
                sender?.initChannel(bluetoothSocket, BluetoothDevice.DEVICE_TYPE_CLASSIC, connectCallBack)

                return BluetoothState.CONNECT_SUCCESS
            }
        }
        return BluetoothState.CONNECT_FAIL
    }

    override fun onPostExecute(target: Context?, result: Int?) {
        super.onPostExecute(target, result)
        if (connectCallBack != null) {
            if (result == BluetoothState.CONNECT_SUCCESS) {
                try {
                    mContext.unregisterReceiver(mReceiver)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val filter = IntentFilter()
                filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                mContext.registerReceiver(mReceiver, filter)
                connectCallBack.onConnected(sender)
            } else {
                connectCallBack.onError(BluetoothState.CONNECT_FAIL, "Connect failed!")
            }
        }
    }


    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED == intent.action) {
                HBluetooth.getInstance(mContext).isConnected = false
                connectCallBack?.onDisConnected()
            }
        }
    }

    init {
        try {
            val method: Method = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                bluetoothDevice.javaClass.getMethod("createInsecureRfcommSocket", Int::class.javaPrimitiveType)
            } else {
                bluetoothDevice.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
            }
            bluetoothSocket = method.invoke(bluetoothDevice, 1) as BluetoothSocket
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }


}