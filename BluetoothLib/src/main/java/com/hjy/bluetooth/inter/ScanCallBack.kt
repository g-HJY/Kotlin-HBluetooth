package com.hjy.bluetooth.inter

import com.hjy.bluetooth.entity.BluetoothDevice

/**
 * Created by _H_JY on 2018/10/20.
 */
interface ScanCallBack {
    fun onScanStart()
    fun onScanning(scannedDevices: List<BluetoothDevice>?, currentScannedDevice: BluetoothDevice?)
    fun onError(errorType: Int, errorMsg: String?)
    fun onScanFinished(bluetoothDevices: List<BluetoothDevice>?)
}