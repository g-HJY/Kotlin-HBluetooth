package com.hjy.bluetooth.entity

/**
 * Created by _H_JY on 2018/10/20.
 */
class BluetoothDevice {
    var name: String? = null
    var type = 0
    var address: String? = null
    var isPaired = false
    var scanRecord: ByteArray? = null

    override fun equals(obj: Any?): Boolean {
        if (obj is BluetoothDevice) {
            return address == obj.address
        }
        return super.equals(obj)
    }

    override fun toString(): String {
        return "BluetoothDevice{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", address='" + address + '\'' +
                ", paired=" + isPaired +
                '}'
    }

    companion object {
        const val DEVICE_TYPE_CLASSIC = android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC
        const val DEVICE_TYPE_LE = android.bluetooth.BluetoothDevice.DEVICE_TYPE_LE
    }
}