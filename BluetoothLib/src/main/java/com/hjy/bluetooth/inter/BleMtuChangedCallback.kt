package com.hjy.bluetooth.inter

import com.hjy.bluetooth.exception.BleException

/**
 * author : HJY
 * date   : 2021/9/9
 * desc   :
 */
interface BleMtuChangedCallback {
    fun onSetMTUFailure(realMtuSize: Int, bleException: BleException?)
    fun onMtuChanged(mtuSize: Int)
}