package com.hjy.test

import android.app.Application
import com.hjy.bluetooth.HBluetooth

/**
 * author : HJY
 * date   : 2021/11/12
 * desc   :
 */
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //初始化 HBluetooth
        HBluetooth.init(this)
    }
}