package com.hjy.bluetooth.utils

import android.bluetooth.BluetoothGattCharacteristic
import com.hjy.bluetooth.HBluetooth.Companion.getInstance
import java.io.DataInputStream

/**
 * author : HJY
 * date   : 2021/11/17
 * desc   :
 */
object ReceiveHolder {

    fun receiveBleReturnData(characteristic: BluetoothGattCharacteristic) {
        val result = characteristic.value
        getInstance().receiver()?.receiveCallBack?.onReceived(null, result)
    }

    fun receiveClassicBluetoothReturnData(dis: DataInputStream?, result: ByteArray?) {
        getInstance().receiver()?.receiveCallBack?.onReceived(dis, result)
    }
}