package com.hjy.bluetooth

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.support.annotation.IntDef
import com.hjy.bluetooth.constant.ValueLimit
import com.hjy.bluetooth.entity.ScanFilter
import com.hjy.bluetooth.exception.BluetoothException
import com.hjy.bluetooth.inter.*
import com.hjy.bluetooth.operator.abstra.Connector
import com.hjy.bluetooth.operator.abstra.Receiver
import com.hjy.bluetooth.operator.abstra.Scanner
import com.hjy.bluetooth.operator.abstra.Sender
import com.hjy.bluetooth.operator.impl.BluetoothConnector
import com.hjy.bluetooth.operator.impl.BluetoothReceiver
import com.hjy.bluetooth.operator.impl.BluetoothScanner
import com.hjy.bluetooth.operator.impl.BluetoothSender

/**
 * Created by _H_JY on 2018/10/20.
 */
class HBluetooth private constructor(private val mContext: Context) {
    private lateinit var mAdapter: BluetoothAdapter
    private lateinit var scanner: Scanner
    private lateinit var connector: Connector
    private lateinit var sender: Sender
    private lateinit var receiver: Receiver
    var isConnected = false
    var bleConfig: BleConfig? = null

    @IntDef(BluetoothDevice.DEVICE_TYPE_CLASSIC.toLong(), BluetoothDevice.DEVICE_TYPE_LE.toLong())
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class BluetoothType

    /**
     * You must call it first after initialize this class.
     *
     * @return
     */
    fun enableBluetooth(): HBluetooth = apply {
        mAdapter = BluetoothAdapter.getDefaultAdapter()

        if (mAdapter == null) {
            throw RuntimeException("Bluetooth unsupported!")
        }
        if (!mAdapter.isEnabled) {
            mAdapter.enable()
        }
        scanner = BluetoothScanner(mContext, mAdapter)
        connector = BluetoothConnector(mContext, mAdapter)
        sender = BluetoothSender()
        receiver = BluetoothReceiver()
    }

    val bondedDevices: Set<BluetoothDevice>?
        get() = if (mAdapter == null) null else mAdapter.bondedDevices

    fun scan(@BluetoothType scanType: Int, scanCallBack: ScanCallBack) {
        scanner?.scan(scanType, scanCallBack)
    }

    fun scan(@BluetoothType scanType: Int, timeUse: Int, scanCallBack: ScanCallBack) {
        scanner?.scan(scanType, timeUse, scanCallBack)
    }

    /**
     * @param scanType
     * @param filter       Accurate or fuzzy matching scanning according to the device name
     * @param scanCallBack
     */
    fun scan(@BluetoothType scanType: Int, filter: ScanFilter?, scanCallBack: ScanCallBack?) {
        if (scanner != null) {
            scanner.setFilter(filter)
            scanner.scan(scanType, scanCallBack!!)
        }
    }

    /**
     * @param scanType
     * @param timeUse
     * @param filter       Accurate or fuzzy matching scanning according to the device name
     * @param scanCallBack
     */
    fun scan(@BluetoothType scanType: Int, timeUse: Int, filter: ScanFilter?, scanCallBack: ScanCallBack?) {
        if (scanner != null) {
            scanner.setFilter(filter)
            scanner.scan(scanType, timeUse, scanCallBack!!)
        }
    }


    fun scanner(): Scanner? {
        checkIfEnableBluetoothFirst()
        return scanner
    }

    @Synchronized
    fun cancelScan() {
        scanner?.stopScan()
    }

    @Synchronized
    fun destroyChannel() {
        sender?.destroyChannel()
    }

    private fun resetCallBack() {
        if (sender != null) {
            sender.resetCallBack()
        }
        if (scanner != null) {
            scanner.resetCallBack()
        }
    }

    fun connector(): Connector? {
        checkIfEnableBluetoothFirst()
        return connector
    }

    fun connect(bluetoothDevice: com.hjy.bluetooth.entity.BluetoothDevice?, connectCallBack: ConnectCallBack?) {
        connector?.connect(bluetoothDevice!!, connectCallBack)
    }

    fun connect(bluetoothDevice: com.hjy.bluetooth.entity.BluetoothDevice?, connectCallBack: ConnectCallBack?, bleNotifyCallBack: BleNotifyCallBack?) {
        connector?.connect(bluetoothDevice, connectCallBack, bleNotifyCallBack)

    }

    fun send(cmd: ByteArray?, sendCallBack: SendCallBack?) {
        sender?.send(cmd!!, sendCallBack)
    }

    fun sender(): Sender? {
        checkIfEnableBluetoothFirst()
        return sender
    }


    fun setReceiver(receiveCallBack: ReceiveCallBack?): Receiver? {
        receiver?.receiveCallBack = receiveCallBack
        return receiver
    }

    fun receiver(): Receiver? {
        checkIfEnableBluetoothFirst()
        return receiver
    }

    private fun checkIfEnableBluetoothFirst() {
        if (mAdapter == null || !mAdapter.isEnabled) {
            throw RuntimeException("you must call enableBluetooth() first.")
        }
    }


    class BleConfig {
        var serviceUUID: String? = null
            private set
        var writeCharacteristicUUID: String? = null
            private set
        var notifyCharacteristicUUID: String? = null
            private set
        var isUseCharacteristicDescriptor = false
            private set
        var mtuSize = 0
            private set
        var sendTimeInterval = 20
            private set
        var eachSplitPacketLen = 20
            private set
        var splitPacketToSendWhenCmdLenBeyond = false
            private set

        var notifyDelay = 200
            private set

        private var mBleMtuChangedCallback: BleMtuChangedCallback? = null

        fun withServiceUUID(serviceUUID: String?): BleConfig = apply {
            this.serviceUUID = serviceUUID
        }

        fun withWriteCharacteristicUUID(writeCharacteristicUUID: String?): BleConfig = apply {
            this.writeCharacteristicUUID = writeCharacteristicUUID
        }

        fun withNotifyCharacteristicUUID(notifyCharacteristicUUID: String?): BleConfig = apply {
            this.notifyCharacteristicUUID = notifyCharacteristicUUID
        }

        fun useCharacteristicDescriptor(useCharacteristicDescriptor: Boolean): BleConfig = apply {
            isUseCharacteristicDescriptor = useCharacteristicDescriptor
        }

        /**
         * @param splitPacketToSendWhenCmdLenBeyond default value = false
         * @param sendTimeInterval                  unit is ms,default value = 20ms
         * The time interval of subcontracting sending shall not be less than 20ms to avoid sending failure
         * The default length of each subcontract is 20 bytes
         * @return
         */
        fun splitPacketToSendWhenCmdLenBeyond(splitPacketToSendWhenCmdLenBeyond: Boolean, sendTimeInterval: Int): BleConfig = apply {
            this.splitPacketToSendWhenCmdLenBeyond = splitPacketToSendWhenCmdLenBeyond
            this.sendTimeInterval = sendTimeInterval
        }

        fun splitPacketToSendWhenCmdLenBeyond(splitPacketToSendWhenCmdLenBeyond: Boolean, sendTimeInterval: Int, eachSplitPacketLen: Int): BleConfig = apply {
            this.splitPacketToSendWhenCmdLenBeyond = splitPacketToSendWhenCmdLenBeyond
            this.sendTimeInterval = sendTimeInterval
            this.eachSplitPacketLen = eachSplitPacketLen
        }

        /**
         * @param splitPacketToSendWhenCmdLenBeyond default value = false
         * sendTimeInterval's unit is ms,default value = 20ms
         * The default length of each subcontract is 20 bytes
         * @return
         */
        fun splitPacketToSendWhenCmdLenBeyond(splitPacketToSendWhenCmdLenBeyond: Boolean): BleConfig = apply {
            this.splitPacketToSendWhenCmdLenBeyond = splitPacketToSendWhenCmdLenBeyond
        }

        /**
         * default value = 200ms
         */
        fun notifyDelay(millisecond: Int): BleConfig = apply {
            this.notifyDelay = millisecond
        }

        /**
         * set Mtu
         *
         * @param mtuSize
         * @param callback
         */
        fun setMtu(mtuSize: Int, callback: BleMtuChangedCallback?): BleConfig = apply {
            requireNotNull(callback) { "BleMtuChangedCallback can not be Null!" }
            if (mtuSize > ValueLimit.DEFAULT_MAX_MTU) {
                callback.onSetMTUFailure(mtuSize, BluetoothException("requiredMtu should lower than 512 !"))
            }
            if (mtuSize < ValueLimit.DEFAULT_MTU) {
                callback.onSetMTUFailure(mtuSize, BluetoothException("requiredMtu should higher than 23 !"))
            }
            this.mtuSize = mtuSize
            mBleMtuChangedCallback = callback
        }

        fun getBleMtuChangedCallback(): BleMtuChangedCallback? {
            return mBleMtuChangedCallback
        }


    }


    @Synchronized
    fun release() {
        cancelScan()
        destroyChannel()
        resetCallBack()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var mHBluetooth: HBluetooth? = null

        @JvmStatic
        fun getInstance(): HBluetooth {
            if (mHBluetooth == null) {
                synchronized(HBluetooth::class) {
                    if (mHBluetooth == null) {
                        mHBluetooth = HBluetooth(getContext())
                    }
                }
            }
            return mHBluetooth!!
        }

        private var APPLICATION_CONTEXT: Context? = null

        /**
         * 反射获取 application context
         */
        private fun getContext(): Context {
            if (null == APPLICATION_CONTEXT) {
                try {
                    val application = Class.forName("android.app.ActivityThread")
                            .getMethod("currentApplication")
                            .invoke(null, null as Array<Any?>?) as Application
                    if (application != null) {
                        APPLICATION_CONTEXT = application
                        return application
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    val application = Class.forName("android.app.AppGlobals")
                            .getMethod("getInitialApplication")
                            .invoke(null, null as Array<Any?>?) as Application
                    if (application != null) {
                        APPLICATION_CONTEXT = application
                        return application
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                throw IllegalStateException("ContextHolder is not initialed, it is recommend to init with application context.")
            }
            return APPLICATION_CONTEXT!!
        }

        /**
         * 初始化context，如果由于不同机型导致反射获取context失败可以在Application调用此方法
         */
        @JvmStatic
        fun init(context: Context?) {
            APPLICATION_CONTEXT = context
        }
    }


}
