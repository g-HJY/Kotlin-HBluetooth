package com.hjy.test

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import com.hjy.bluetooth.HBluetooth
import com.hjy.bluetooth.entity.BluetoothDevice
import com.hjy.bluetooth.exception.BleException
import com.hjy.bluetooth.inter.BleMtuChangedCallback
import com.hjy.bluetooth.inter.ConnectCallBack
import com.hjy.bluetooth.inter.ScanCallBack
import com.hjy.bluetooth.inter.SendCallBack
import com.hjy.bluetooth.operator.abstra.Sender
import java.io.DataInputStream
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener, OnItemClickListener {
    private var listView: ListView? = null
    private val list: MutableList<BluetoothDevice> = ArrayList()
    private var adapter: MyAdapter? = null
    private lateinit var mHBluetooth: HBluetooth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn_scan_classic).setOnClickListener(this)
        findViewById<View>(R.id.btn_scan_ble).setOnClickListener(this)
        findViewById<View>(R.id.btn_disconnect).setOnClickListener(this)
        listView = findViewById(R.id.listView)
        adapter = MyAdapter(this, list)
        listView?.adapter = adapter
        listView?.onItemClickListener = this
        mHBluetooth = HBluetooth.getInstance(this)
        mHBluetooth //开启蓝牙功能
                .enableBluetooth() //低功耗蓝牙才需要设置，传入你自己的UUID
                .setWriteCharacteristicUUID("0000fe61-0000-1000-8000-00805f9b34fb") //设置MTU扩容
                .setMtu(200, object : BleMtuChangedCallback {

                    override fun onSetMTUFailure(realMtuSize: Int, bleException: BleException?) {
                        Log.i(TAG, "bleException:" + bleException?.message + "  realMtuSize:" + realMtuSize)
                    }

                    override fun onMtuChanged(mtuSize: Int) {}
                })


    }

    override fun onDestroy() {
        super.onDestroy()
        mHBluetooth.release()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_disconnect) {
            mHBluetooth.release()
        } else {
            if (list != null && list.size > 0) {
                list.clear()
                adapter!!.notifyDataSetChanged()
            }
            var type = 0
            if (view.id == R.id.btn_scan_classic) {
                type = BluetoothDevice.DEVICE_TYPE_CLASSIC
            } else if (view.id == R.id.btn_scan_ble) {
                type = BluetoothDevice.DEVICE_TYPE_LE

                //如果没有设置扫描时间，低功耗蓝牙扫描需要手动调用stopScan()方法停止扫描，否则会一直扫描下去
//                Handler().postDelayed({
//                    mHBluetooth.scanner().stopScan()
//                }, 10000)
            }
            val setScanTimeUse = true
            if (setScanTimeUse) {
                //有设置扫描时间的扫描，时间到会自动结束扫描
                scanWithTimeUse(type)
            } else {
                //扫描蓝牙设备,没有设置扫描时间,低功耗蓝牙会一直扫描下去
                mHBluetooth.scan(type, object : ScanCallBack {
                    override fun onScanStart() {
                        Log.i(TAG, "开始扫描")
                    }

                    override fun onScanning(scannedDevices: List<BluetoothDevice>?, currentScannedDevice: BluetoothDevice?) {
                        Log.i(TAG, "扫描中")
                        if (scannedDevices != null && scannedDevices.size > 0) {
                            list!!.clear()
                            list.addAll(scannedDevices)
                            adapter!!.notifyDataSetChanged()
                        }
                    }

                    override fun onError(errorType: Int, errorMsg: String?) {}
                    override fun onScanFinished(bluetoothDevices: List<BluetoothDevice>?) {
                        Log.i(TAG, "扫描结束")
                        if (bluetoothDevices != null && bluetoothDevices.size > 0) {
                            list!!.clear()
                            list.addAll(bluetoothDevices)
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                })
            }
        }
    }

    override fun onItemClick(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
        val device = list!![i]
        //调用连接器连接蓝牙设备
        mHBluetooth.connector()
                ?.connect(device, object : ConnectCallBack {
                    override fun onConnecting() {
                        Log.i(TAG, "连接中...")
                    }

                    override fun onConnected(sender: Sender?) {
                        Log.i(TAG, "连接成功,isConnected:" + mHBluetooth!!.isConnected)
                        //调用发送器发送命令
                        sender?.send(byteArrayOf(0x01, 0x02), object : SendCallBack {
                            override fun onSending() {
                                Log.i(TAG, "命令发送中...")
                            }

                            override fun onReceived(dataInputStream: DataInputStream?, bleValue: ByteArray) {
                                Log.i(TAG, "onReceived->$dataInputStream---$bleValue")
                            }
                        })
                    }

                    override fun onDisConnecting() {
                        Log.i(TAG, "断开连接中...")
                    }

                    override fun onDisConnected() {
                        Log.i(TAG, "已断开连接,isConnected:" + mHBluetooth!!.isConnected)
                    }

                    override fun onError(errorType: Int, errorMsg: String) {
                        Log.i(TAG, "错误类型：$errorType 错误原因：$errorMsg")
                    }
                })
    }

    private fun scanWithTimeUse(type: Int) {
        //扫描蓝牙设备，扫描6秒就自动停止扫描
        mHBluetooth.scan(type, 6000, object : ScanCallBack {
            override fun onScanStart() {
                Log.i(TAG, "开始扫描")
            }

            override fun onScanning(scannedDevices: List<BluetoothDevice>?, currentScannedDevice: BluetoothDevice?) {
                Log.i(TAG, "扫描中")
                if (scannedDevices != null && scannedDevices.size > 0) {
                    list!!.clear()
                    list.addAll(scannedDevices)
                    adapter!!.notifyDataSetChanged()
                }
            }

            override fun onError(errorType: Int, errorMsg: String?) {}
            override fun onScanFinished(bluetoothDevices: List<BluetoothDevice>?) {
                Log.i(TAG, "扫描结束")
                if (bluetoothDevices != null && bluetoothDevices.size > 0) {
                    list!!.clear()
                    list.addAll(bluetoothDevices)
                    adapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    companion object {
        private const val TAG = "mylog"
    }
}