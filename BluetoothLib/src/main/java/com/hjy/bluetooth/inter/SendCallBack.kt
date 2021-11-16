package com.hjy.bluetooth.inter

import com.hjy.bluetooth.exception.BluetoothException

/**
 * Created by _H_JY on 2018/10/24.
 */
interface SendCallBack {
    fun onSending(command: ByteArray?)
    fun onSendFailure(bluetoothException: BluetoothException)
}