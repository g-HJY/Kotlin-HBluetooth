package com.hjy.bluetooth.operator.abstra

import com.hjy.bluetooth.inter.ScanCallBack

/**
 * Created by _H_JY on 2018/10/20.
 */
abstract class Scanner {
    abstract fun scan(scanType: Int, scanCallBack: ScanCallBack)
    abstract fun scan(scanType: Int, timeUse: Int, scanCallBack: ScanCallBack)
    abstract fun stopScan()
}