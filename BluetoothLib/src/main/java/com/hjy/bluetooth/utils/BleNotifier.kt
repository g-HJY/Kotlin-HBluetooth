package com.hjy.bluetooth.utils


import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.text.TextUtils
import com.hjy.bluetooth.HBluetooth
import com.hjy.bluetooth.exception.BleException
import com.hjy.bluetooth.inter.BleNotifyCallBack
import java.util.*

/**
 * author : HJY
 * date   : 2021/11/11
 * desc   :
 */
object BleNotifier {


    //Open the ble notification
    fun openNotification(ctx: Context?, gatt: BluetoothGatt, service: BluetoothGattService, notifyUUID: String?,
                         bluetoothGattCharacteristic: BluetoothGattCharacteristic, bleNotifyCallBack: BleNotifyCallBack?) {
        //Turn on the Android terminal to receive notifications
        var finalNotifyCharacteristic: BluetoothGattCharacteristic? = null
        var notifySuccess: Boolean
        var failureMsg: String
        if (!TextUtils.isEmpty(notifyUUID)) {
            val notifyCharacteristic = service.getCharacteristic(UUID.fromString(notifyUUID))
            if (notifyCharacteristic != null) {
                notifySuccess = gatt.setCharacteristicNotification(notifyCharacteristic.also { finalNotifyCharacteristic = it }, true)
                failureMsg = "Gatt setCharacteristicNotification fail"
            } else {
                notifySuccess = false
                failureMsg = "NotificationCharacteristic is null,please check the notifyUUID whether right"
            }
        } else {
            notifySuccess = gatt.setCharacteristicNotification(bluetoothGattCharacteristic.also { finalNotifyCharacteristic = it }, true)
            failureMsg = "Gatt setCharacteristicNotification fail"
        }
        if (!notifySuccess) {
            bleNotifyCallBack?.onNotifyFailure(BleException(failureMsg))
            return
        }

        //Write the data switch of on notification to the descriptor attribute of characteristic,
        //so that when the hardware data changes,
        //it can actively send data to the mobile phone
        var descriptor: BluetoothGattDescriptor? = null
        var useCharacteristicDescriptor = false
        val bleConfig: HBluetooth.BleConfig? = HBluetooth.getInstance(ctx!!).mBleConfig
        if (bleConfig != null) {
            useCharacteristicDescriptor = bleConfig.isUseCharacteristicDescriptor
        }
        if (finalNotifyCharacteristic != null) {
            descriptor = if (useCharacteristicDescriptor) {
                finalNotifyCharacteristic!!.getDescriptor(finalNotifyCharacteristic!!.uuid)
            } else {
                finalNotifyCharacteristic!!.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            }
        }
        if (descriptor != null) {
            if (useCharacteristicDescriptor) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                notifySuccess = gatt.writeDescriptor(descriptor)
                failureMsg = "Write descriptor fail"
            } else {
                notifySuccess = true
            }
        } else {
            notifySuccess = false
            failureMsg = "Descriptor is null,please check whether support use characteristicDescriptor"
        }
        if (bleNotifyCallBack != null) {
            if (notifySuccess) {
                bleNotifyCallBack.onNotifySuccess()
            } else {
                bleNotifyCallBack.onNotifyFailure(BleException(failureMsg))
            }
        }
    }
}