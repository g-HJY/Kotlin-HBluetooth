package com.hjy.bluetooth.utils

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.hjy.bluetooth.HBluetooth
import com.hjy.bluetooth.HBluetooth.BleConfig
import com.hjy.bluetooth.HBluetooth.Companion.getInstance
import com.hjy.bluetooth.exception.BluetoothException
import com.hjy.bluetooth.inter.BleNotifyCallBack
import com.hjy.bluetooth.operator.impl.BluetoothReceiver
import java.util.*

/**
 * author : HJY
 * date   : 2021/11/11
 * desc   :
 */
object BleNotifier {
    /**
     * Open the ble notification
     *
     * @param gatt
     * @param service
     * @param notifyUUID
     * @param bluetoothGattCharacteristic
     * @param bleNotifyCallBack
     */
    fun openNotification(gatt: BluetoothGatt, service: BluetoothGattService, notifyUUID: String?,
                         bluetoothGattCharacteristic: BluetoothGattCharacteristic, bleNotifyCallBack: BleNotifyCallBack?) {

        var notifyDelay = 200
        val bleConfig = getInstance().bleConfig
        if (bleConfig != null) {
            notifyDelay = bleConfig.notifyDelay
        }
        Handler(Looper.getMainLooper()).postDelayed(Runnable { //Turn on the Android terminal to receive notifications
            var finalNotifyCharacteristic: BluetoothGattCharacteristic? = null
            var notifySuccess: Boolean
            var failureMsg: String
            if (!TextUtils.isEmpty(notifyUUID)) {
                val notifyCharacteristic: BluetoothGattCharacteristic = service.getCharacteristic(UUID.fromString(notifyUUID))
                if (notifyCharacteristic != null) {
                    notifySuccess = gatt.setCharacteristicNotification(notifyCharacteristic.also({ finalNotifyCharacteristic = it }), true)
                    failureMsg = "Gatt setCharacteristicNotification fail"
                } else {
                    notifySuccess = false
                    failureMsg = "NotificationCharacteristic is null,please check the notifyUUID whether right"
                }
            } else {
                notifySuccess = gatt.setCharacteristicNotification(bluetoothGattCharacteristic.also({ finalNotifyCharacteristic = it }), true)
                failureMsg = "Gatt setCharacteristicNotification fail"
            }
            if (!notifySuccess) {
                bleNotifyCallBack?.onNotifyFailure(BluetoothException(failureMsg))
                return@Runnable
            }

            //Write the data switch of on notification to the descriptor attribute of characteristic,
            //so that when the hardware data changes,
            //it can actively send data to the mobile phone
            var descriptor: BluetoothGattDescriptor? = null
            var useCharacteristicDescriptor = false
            val bleConfig: HBluetooth.BleConfig? = HBluetooth.getInstance().bleConfig
            if (bleConfig != null) {
                useCharacteristicDescriptor = bleConfig.isUseCharacteristicDescriptor
            }

            finalNotifyCharacteristic?.let {
                descriptor = if (useCharacteristicDescriptor) {
                    it.getDescriptor(it.uuid)
                } else {
                    it.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                }
            }

            descriptor?.let {
                it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                notifySuccess = gatt.writeDescriptor(descriptor)
                failureMsg = "Write descriptor fail"
            }?:let {
                notifySuccess = false
                failureMsg = "Descriptor is null,please check whether support use characteristicDescriptor"
            }

            if (notifySuccess) {
                val receiver: BluetoothReceiver? = HBluetooth.getInstance().receiver() as BluetoothReceiver?
                if (receiver != null) {
                    receiver.finalNotifyDescriptor = descriptor
                }
                bleNotifyCallBack?.onNotifySuccess()
            } else {
                bleNotifyCallBack?.onNotifyFailure(BluetoothException(failureMsg))
            }
        }, notifyDelay.toLong())
    }

    /**
     * Close the ble notification
     */
    fun closeNotification() {
        val receiver: BluetoothReceiver? = HBluetooth.getInstance().receiver() as? BluetoothReceiver?
        receiver?.let {
            val bluetoothGattDescriptor: BluetoothGattDescriptor? = it.finalNotifyDescriptor
            if (bluetoothGattDescriptor != null) {
                bluetoothGattDescriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                it.finalNotifyDescriptor = null
            }
        }

    }
}