package com.hjy.bluetooth.operator.impl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import com.hjy.bluetooth.entity.BluetoothDevice
import com.hjy.bluetooth.inter.ScanCallBack
import com.hjy.bluetooth.operator.abstra.Scanner
import com.hjy.bluetooth.utils.ScanFilterUtils.isInFilter
import java.util.*

/**
 * Created by _H_JY on 2018/10/20.
 */
class BluetoothScanner : Scanner {
    private var scanType = 0
    private var isScanning = false
    private var mContext: Context
    private var scanCallBack: ScanCallBack? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private lateinit var bluetoothDevices: MutableList<BluetoothDevice>
    private var bluetoothAdapter: BluetoothAdapter
    private var handler: Handler? = null

    constructor(context: Context, bluetoothAdapter: BluetoothAdapter) {
        mContext = context
        this.bluetoothAdapter = bluetoothAdapter
    }

    @Synchronized
    override fun scan(scanType: Int, scanCallBack: ScanCallBack) {
        startScan(scanType, 0, scanCallBack)
    }

    @Synchronized
    override fun scan(scanType: Int, timeUse: Int, scanCallBack: ScanCallBack) {
        startScan(scanType, timeUse, scanCallBack)
    }

    private fun startScan(scanType: Int, timeUse: Int, scanCallBack: ScanCallBack) {
        //Important, If we're already discovering or scanning, stop it!
        isScanning = false
        stopScan()

        this.scanType = scanType
        this.scanCallBack = scanCallBack
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this.scanCallBack?.onError(1, "Only system versions above Android 4.3 are supported.")
            return
        }
        if (this.scanType == BluetoothDevice.DEVICE_TYPE_LE && !mContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            this.scanCallBack?.onError(2, "Your device does not support low-power Bluetooth.")
            return
        }

        bluetoothDevices = ArrayList()


        this.scanCallBack?.onScanStart()

        if (this.scanType == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
            unregisterReceiver()

            // Register for broadcasts when a device is discovered
            // Register for broadcasts when discovery has finished
            val filter = IntentFilter().apply {
                addAction(android.bluetooth.BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            mContext.registerReceiver(mReceiver, filter)

            // If we're already discovering, stop it
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }

            if (!bluetoothAdapter.startDiscovery().also { isScanning = it }) {
                this.scanCallBack?.onError(3, "Start discovery fail,make sure you have Bluetooth enabled or open permissions")
            }

        } else if (this.scanType == BluetoothDevice.DEVICE_TYPE_LE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //After 5.0 use BluetoothLeScanner to scan
                //Because bluetoothAdapter.startLeScan deprecated
                bluetoothAdapter.bluetoothLeScanner?.let {
                    bluetoothLeScanner = it
                    isScanning = true
                    bluetoothLeScanner?.startScan(mScanCallback)
                } ?: let {
                    this.scanCallBack?.onError(3, "BluetoothLeScanner is null,make sure you have Bluetooth enabled or open permissions")
                }

            } else if (!bluetoothAdapter.startLeScan(mLeScanCallBack).also { isScanning = it }) {
                this.scanCallBack?.onError(3, "StartLeScan fail,make sure you have Bluetooth enabled or open permissions")
            }
        }

        //Auto stop scan when time out
        if (timeUse != 0) {
            if (handler == null) {
                handler = Handler(Looper.getMainLooper())
            }
            handler?.postDelayed({ stopScan() }, timeUse.toLong())
        }
    }

    //CLASSIC BLUETOOTH
    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // When discovery finds a device
            if (android.bluetooth.BluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from the Intent
                val device = intent
                        .getParcelableExtra<android.bluetooth.BluetoothDevice>(android.bluetooth.BluetoothDevice.EXTRA_DEVICE)

                //If there is filtering, filter the scanning results
                if (getFilter() != null && !isInFilter(device.name, getFilter()!!)) {
                    return
                }

                val rssi = intent.getShortExtra(android.bluetooth.BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()

                // new device found
                val bluetoothDevice = BluetoothDevice().apply {
                    isPaired = (device.bondState == android.bluetooth.BluetoothDevice.BOND_BONDED)
                    address = device.address
                    name = device.name
                    type = device.type
                    this.rssi = rssi
                }

                if (bluetoothDevices != null && bluetoothDevices.size > 0) {
                    if (bluetoothDevices.contains(bluetoothDevice)) {
                        return
                    }
                }
                bluetoothDevices.add(bluetoothDevice)
                scanCallBack?.onScanning(bluetoothDevices, bluetoothDevice)


                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                scanCallBack?.onScanFinished(bluetoothDevices)

                isScanning = false
            }
        }
    }

    //Ble scan callback before 5.0
    private val mLeScanCallBack = LeScanCallback { bluetoothDevice, rssi, bytes ->
        //If there is filtering, filter the scanning results
        if (getFilter() != null && !isInFilter(bluetoothDevice.name, getFilter()!!)) {
            return@LeScanCallback
        }
        handleBleScanResult(bluetoothDevice, rssi, bytes)
    }

    //Ble scan callback after 5.0
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val mScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val bluetoothDevice = result.device
            //If there is filtering, filter the scanning results
            if (getFilter() != null && !isInFilter(bluetoothDevice.name, getFilter()!!)) {
                return
            }
            handleBleScanResult(bluetoothDevice, result.rssi,
                    result.scanRecord?.bytes)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            scanCallBack?.onError(errorCode, "Scan failed!")
        }
    }

    private fun handleBleScanResult(bluetoothDevice: android.bluetooth.BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
        val device = BluetoothDevice().apply {
            name = bluetoothDevice.name
            address = bluetoothDevice.address
            type = BluetoothDevice.DEVICE_TYPE_LE
            isPaired = bluetoothDevice.bondState == android.bluetooth.BluetoothDevice.BOND_BONDED
            this.rssi = rssi
            this.scanRecord = scanRecord
        }
        if (bluetoothDevices.contains(device)) {
            val index = bluetoothDevices.indexOf(device)
            bluetoothDevices[index] = device
        } else {
            bluetoothDevices.add(device)
        }

        scanCallBack?.let {
            if (handler == null) {
                handler = Handler(Looper.getMainLooper())
            }
            handler?.post { it.onScanning(bluetoothDevices, device) }
        }
    }

    @Synchronized
    override fun stopScan() {
        if (scanType == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            unregisterReceiver()
        } else if (scanType == BluetoothDevice.DEVICE_TYPE_LE && bluetoothAdapter.state == BluetoothAdapter.STATE_ON) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothLeScanner?.stopScan(mScanCallback)
            } else {
                bluetoothAdapter.stopLeScan(mLeScanCallBack)
            }
        }

        //If scanning,call back onScanFinished
        if (isScanning) {
            scanCallBack?.let {
                if (handler == null) {
                    handler = Handler(Looper.getMainLooper())
                }
                handler?.post { it.onScanFinished(bluetoothDevices) }
            }
            isScanning = false
        }

    }

    override fun resetCallBack() {
        scanCallBack = null
    }

    private fun unregisterReceiver() {
        try {
            mContext.unregisterReceiver(mReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}