package com.hjy.bluetooth.inter

import java.io.DataInputStream

/**
 * Created by _H_JY on 2018/10/24.
 */
interface SendCallBack {
    fun onSending()
    fun onReceived(dataInputStream: DataInputStream?, bleValue: ByteArray)
}