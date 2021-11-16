package com.hjy.bluetooth.operator.abstra

import com.hjy.bluetooth.entity.ScanFilter
import com.hjy.bluetooth.inter.ScanCallBack

/**
 * Created by _H_JY on 2018/10/20.
 */
abstract class Scanner {
    private var filter: ScanFilter? = null
    abstract fun scan(scanType: Int, scanCallBack: ScanCallBack)
    abstract fun scan(scanType: Int, timeUse: Int, scanCallBack: ScanCallBack)
    abstract fun stopScan()
    abstract fun resetCallBack()

    open fun getFilter(): ScanFilter? {
        return filter
    }

    open fun setFilter(filter: ScanFilter?) {
        this.filter = filter
    }
}