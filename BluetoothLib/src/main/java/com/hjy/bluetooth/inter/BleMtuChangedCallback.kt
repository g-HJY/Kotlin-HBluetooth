package com.hjy.bluetooth.inter

import com.hjy.bluetooth.exception.BluetoothException

/**
 * author : HJY
 * date   : 2021/9/9
 * desc   :
 */
interface BleMtuChangedCallback {
    fun onSetMTUFailure(realMtuSize: Int, bluetoothException: BluetoothException?)
    fun onMtuChanged(mtuSize: Int)
}