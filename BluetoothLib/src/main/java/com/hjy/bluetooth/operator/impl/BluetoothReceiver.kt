package com.hjy.bluetooth.operator.impl

import android.bluetooth.BluetoothGattDescriptor
import com.hjy.bluetooth.inter.ReceiveCallBack
import com.hjy.bluetooth.operator.abstra.Receiver

/**
 * author : HJY
 * date   : 2021/11/12
 * desc   :
 */
class BluetoothReceiver : Receiver() {
    override var receiveCallBack: ReceiveCallBack? = null
    var finalNotifyDescriptor: BluetoothGattDescriptor? = null
}