package com.hjy.bluetooth.utils

import android.text.TextUtils
import com.hjy.bluetooth.entity.ScanFilter

/**
 * author : HJY
 * date   : 2021/11/15
 * desc   :
 */
object ScanFilterUtils {
    fun isInFilter(deviceName: String?, filter: ScanFilter): Boolean {
        if (TextUtils.isEmpty(deviceName)) {
            return false
        }
        val names: Array<String> = filter.names
        if (names != null && names.isNotEmpty()) {
            if (filter.isFuzzyMatching) {
                for (name in names) {
                    if (deviceName!!.contains(name)) {
                        return true
                    }
                }
            } else {
                for (name in names) {
                    if (name == deviceName) {
                        return true
                    }
                }
            }
        }
        return false
    }
}