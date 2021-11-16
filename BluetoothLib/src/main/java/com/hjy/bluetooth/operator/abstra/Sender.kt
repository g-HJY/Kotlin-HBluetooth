package com.hjy.bluetooth.operator.abstra

import com.hjy.bluetooth.inter.ConnectCallBack
import com.hjy.bluetooth.inter.SendCallBack

/**
 * Created by _H_JY on 2018/10/22.
 */
abstract class Sender {
    abstract fun send(command: ByteArray, sendCallBack: SendCallBack?)
    abstract fun <T> initChannel(o: T, type: Int, connectCallBack: ConnectCallBack?): T?
    abstract fun discoverServices()
    abstract fun <G> initSenderHelper(g: G): G?
    abstract fun destroyChannel()
    abstract fun resetCallBack()
}