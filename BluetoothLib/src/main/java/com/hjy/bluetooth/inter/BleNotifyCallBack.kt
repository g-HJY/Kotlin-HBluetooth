package com.hjy.bluetooth.inter

import com.hjy.bluetooth.exception.BleException

/**
 * author : HJY
 * date   : 2021/11/10
 * desc   :
 */
interface BleNotifyCallBack {
    fun onNotifySuccess()
    fun onNotifyFailure(bleException: BleException?)
}