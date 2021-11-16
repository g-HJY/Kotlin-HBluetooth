package com.hjy.bluetooth.operator.abstra

import com.hjy.bluetooth.inter.ReceiveCallBack

/**
 * author : HJY
 * date   : 2021/11/12
 * desc   :
 */
abstract class Receiver {
    abstract var receiveCallBack: ReceiveCallBack?
}