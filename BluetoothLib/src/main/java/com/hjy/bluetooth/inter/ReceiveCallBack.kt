package com.hjy.bluetooth.inter

import java.io.DataInputStream

/**
 * Created by _H_JY on 2018/10/24.
 */
interface ReceiveCallBack {
    fun onReceived(dataInputStream: DataInputStream?, result: ByteArray?)
}