package com.hjy.bluetooth.inter

import com.hjy.bluetooth.operator.abstra.Sender

/**
 * Created by _H_JY on 2018/10/20.
 */
interface ConnectCallBack {
    fun onConnecting()
    fun onConnected(sender: Sender?)
    fun onDisConnecting()
    fun onDisConnected()
    fun onError(errorType: Int, errorMsg: String)
}