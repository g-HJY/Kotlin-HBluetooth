package com.hjy.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.support.annotation.IntDef
import com.hjy.bluetooth.constant.ValueLimit
import com.hjy.bluetooth.exception.BleException
import com.hjy.bluetooth.inter.BleMtuChangedCallback
import com.hjy.bluetooth.inter.ScanCallBack
import com.hjy.bluetooth.operator.abstra.Connector
import com.hjy.bluetooth.operator.abstra.Scanner
import com.hjy.bluetooth.operator.abstra.Sender
import com.hjy.bluetooth.operator.impl.BluetoothConnector
import com.hjy.bluetooth.operator.impl.BluetoothScanner
import com.hjy.bluetooth.operator.impl.BluetoothSender

/**
 * Created by _H_JY on 2018/10/20.
 */
class HBluetooth private constructor(private val mContext: Context) {
    private var mAdapter: BluetoothAdapter? = null
    private var scanner: Scanner? = null
    private var connector: Connector? = null
    private var sender: Sender? = null
    var isConnected = false
    var mtuSize = 0
        private set
    var bleMtuChangedCallback: BleMtuChangedCallback? = null
        private set
    var writeCharacteristicUUID: String? = null
        private set

    @IntDef(BluetoothDevice.DEVICE_TYPE_CLASSIC.toLong(), BluetoothDevice.DEVICE_TYPE_LE.toLong())
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class BluetoothType

    /**
     * You must call it first after initialize this class.
     *
     * @return
     */
    fun enableBluetooth(): HBluetooth {
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mAdapter == null) {
            throw RuntimeException("Bluetooth unsupported!")
        }
        if (!mAdapter!!.isEnabled) {
            mAdapter!!.enable()
        }
        scanner = BluetoothScanner(mContext, mAdapter)
        connector = BluetoothConnector(mContext, mAdapter)
        sender = BluetoothSender()
        return this
    }

    val bondedDevices: Set<BluetoothDevice>?
        get() = if (mAdapter == null) null else mAdapter!!.bondedDevices

    fun scan(@BluetoothType scanType: Int, scanCallBack: ScanCallBack) {
        if (scanner != null) {
            scanner!!.scan(scanType, scanCallBack)
        }
    }

    fun scan(@BluetoothType scanType: Int, timeUse: Int, scanCallBack: ScanCallBack) {
        if (scanner != null) {
            scanner!!.scan(scanType, timeUse, scanCallBack)
        }
    }

    fun scanner(): Scanner? {
        if (mAdapter == null || !mAdapter!!.isEnabled) {
            throw RuntimeException("you must call enableBluetooth() first.")
        }
        return scanner
    }

    @Synchronized
    fun cancelScan() {
        if (scanner != null) {
            scanner!!.stopScan()
        }
    }

    @Synchronized
    fun destroyChannel() {
        if (sender != null) {
            sender!!.destroyChannel()
        }
    }

    fun connector(): Connector? {
        if (mAdapter == null || !mAdapter!!.isEnabled) {
            throw RuntimeException("you must call enableBluetooth() first.")
        }
        return connector
    }

    fun sender(): Sender? {
        if (mAdapter == null || !mAdapter!!.isEnabled) {
            throw RuntimeException("you must call enableBluetooth() first.")
        }
        return sender
    }

    fun setWriteCharacteristicUUID(writeCharacteristicUUID: String?): HBluetooth {
        this.writeCharacteristicUUID = writeCharacteristicUUID
        return this
    }

    /**
     * set Mtu
     *
     * @param mtuSize
     * @param callback
     */
    fun setMtu(mtuSize: Int,
               callback: BleMtuChangedCallback?): HBluetooth {
        requireNotNull(callback) { "BleMtuChangedCallback can not be Null!" }
        if (mtuSize > ValueLimit.DEFAULT_MAX_MTU) {
            callback.onSetMTUFailure(mtuSize, BleException("requiredMtu should lower than 512 !"))
        }
        if (mtuSize < ValueLimit.DEFAULT_MTU) {
            callback.onSetMTUFailure(mtuSize, BleException("requiredMtu should higher than 23 !"))
        }
        this.mtuSize = mtuSize
        bleMtuChangedCallback = callback
        return this
    }

    @Synchronized
    fun release() {
        cancelScan()
        destroyChannel()
    }

    companion object {
        @Volatile
        private var mHBluetooth: HBluetooth? = null

        fun getInstance(context: Context): HBluetooth {
            if (mHBluetooth == null) {
                synchronized(HBluetooth::class) {
                    if (mHBluetooth == null) {
                        mHBluetooth = HBluetooth(context.applicationContext)
                    }
                }
            }
            return mHBluetooth!!
        }
    }

}