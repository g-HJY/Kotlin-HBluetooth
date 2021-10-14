package com.hjy.bluetooth.operator.abstra

import com.hjy.bluetooth.entity.BluetoothDevice
import com.hjy.bluetooth.inter.ConnectCallBack

/**
 * Created by _H_JY on 2018/10/20.
 */
abstract class Connector {
    abstract fun connect(device: BluetoothDevice, connectCallBack: ConnectCallBack?)
}