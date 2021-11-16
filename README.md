# HBluetooth
封装了支持经典蓝牙和低功耗蓝牙扫描，连接，以及通信的库。附带使用例子。该库后续会持续升级维护，敬请关注...

一.集成方式

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
 
 Step 2. Add the dependency

	dependencies {
	     implementation 'com.github.g-HJY:Kotlin-HBluetooth:V1.3.1'
	}


二.使用介绍
1.第一步，使用前先在你应用的Application中调init方法初始化HBluetooth：
               class MyApp : Application() {
                   override fun onCreate() {
                       super.onCreate()
                       //初始化 HBluetooth
                       HBluetooth.init(this)
                   }
               }



2.然后必须调用enableBluetooth()方法开启蓝牙功能，你可以在activity中调用：

               HBluetooth.getInstance().enableBluetooth()




3.如果是低功耗蓝牙，需要设置配置项，经典蓝牙忽略跳过这一步即可：

分别是主服务UUID（withServiceUUID）、读写特征值UUID（withWriteCharacteristicUUID）、通知UUID（withNotifyCharacteristicUUID）以及是否设置最大传输单元（setMtu不设置不用调）等；
您还可以设置分包发送的时间间隔和包长度

        //请填写你自己设备的UUID
        //低功耗蓝牙才需要配置mHBluetooth.bleConfig = ...
        mHBluetooth.bleConfig = BleConfig().apply {
            withServiceUUID("0000fe61-0000-1000-8000-00805f9b34fb")
            withWriteCharacteristicUUID("0000fe61-0000-1000-8000-00805f9b34fb")
            withNotifyCharacteristicUUID("0000fe61-0000-1000-8000-00805f9b34fb") //useCharacteristicDescriptor 默认为false
            //默认为false
            //useCharacteristicDescriptor(false)
            //splitPacketToSendWhenCmdLenBeyond(false)
            //连接后开启通知的延迟时间，单位ms，默认200ms
            //notifyDelay(200)
            setMtu(200, object : BleMtuChangedCallback {
                override fun onSetMTUFailure(realMtuSize: Int, bluetoothException: BluetoothException?) {
                    Log.i(TAG, "bluetoothException:" + bluetoothException?.message + "  realMtuSize:" + realMtuSize)
                }

                override fun onMtuChanged(mtuSize: Int) {
                    Log.i(TAG, "Mtu set success,mtuSize:$mtuSize")
                }
            })
        }


4.开启蓝牙能力后，接着你就可以开始进行蓝牙设备扫描，其中，type 为蓝牙设备类型（经典蓝牙或低功耗蓝牙）：

               HBluetooth.getInstance()
                    .scan(type, object : ScanCallBack {
                    override fun onScanStart() {
                        Log.i(TAG, "开始扫描")
                    }

                    override fun onScanning(scannedDevices: List<BluetoothDevice>?, currentScannedDevice: BluetoothDevice?) {
                        Log.i(TAG, "扫描中")
                        if (scannedDevices != null && scannedDevices.size > 0) {
                            list!!.clear()
                            list.addAll(scannedDevices)
                            adapter!!.notifyDataSetChanged()
                        }
                    }

                    override fun onError(errorType: Int, errorMsg: String?) {}
                    override fun onScanFinished(bluetoothDevices: List<BluetoothDevice>?) {
                        Log.i(TAG, "扫描结束")
                        if (bluetoothDevices != null && bluetoothDevices.size > 0) {
                            list!!.clear()
                            list.addAll(bluetoothDevices)
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                })


    或者，如果你想在第一步操作后直接进行扫描，则可以这样调用：
            HBluetooth.getInstance()
                    .enableBluetooth()
                    .scan(type, object : ScanCallBack {
                    override fun onScanStart() {
                        Log.i(TAG, "开始扫描")
                    }

                    override fun onScanning(scannedDevices: List<BluetoothDevice>?, currentScannedDevice: BluetoothDevice?) {
                        Log.i(TAG, "扫描中")
                        if (scannedDevices != null && scannedDevices.size > 0) {
                            list!!.clear()
                            list.addAll(scannedDevices)
                            adapter!!.notifyDataSetChanged()
                        }
                    }

                    override fun onError(errorType: Int, errorMsg: String?) {}
                    override fun onScanFinished(bluetoothDevices: List<BluetoothDevice>?) {
                        Log.i(TAG, "扫描结束")
                        if (bluetoothDevices != null && bluetoothDevices.size > 0) {
                            list!!.clear()
                            list.addAll(bluetoothDevices)
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                })



5.一旦扫描到设备，你就可以找到目标设备并连接：

               HBluetooth.getInstance()
              .connect(device, object : ConnectCallBack {
                                  override fun onConnecting() {
                                      Log.i(TAG, "连接中...")
                                  }

                                  override fun onConnected(sender: Sender?) {
                                      Log.i(TAG, "连接成功,isConnected:" + mHBluetooth.isConnected)
                                     //调用发送器发送命令,仅为模拟示范代码
                                      sender?.send(byteArrayOf(0x01, 0x02), object : SendCallBack {
                                          override fun onSending(command:ByteArray?) {
                                              Log.i(TAG, "命令发送中...")
                                          }

                                          override fun onSendFailure(bluetoothException: BluetoothException) {
                                              Log.i(TAG, "命令发送失败，bluetoothException:${bluetoothException.message}")
                                          }
                                      })
                                  }

                                  override fun onDisConnecting() {
                                      Log.i(TAG, "断开连接中...")
                                  }

                                  override fun onDisConnected() {
                                      Log.i(TAG, "已断开连接,isConnected:" + mHBluetooth.isConnected)
                                  }

                                  override fun onError(errorType: Int, errorMsg: String) {
                                      Log.i(TAG, "错误类型：$errorType 错误原因：$errorMsg")
                                  }
                                  //低功耗蓝牙才需要BleNotifyCallBack
                                  //经典蓝牙可以只调两参方法connect(BluetoothDevice device, ConnectCallBack connectCallBack)
                              }, object : BleNotifyCallBack {
                                  override fun onNotifySuccess() {
                                      Log.i(TAG, "打开通知成功")
                                  }

                                  override fun onNotifyFailure(bluetoothException: BluetoothException?) {
                                      Log.i(TAG, "打开通知失败：${bluetoothException?.message}")
                                  }

                              })


 6.设备连接成功后，你可以开始与设备进行通信：

               HBluetooth.getInstance()
                       .send(byteArrayOf(0x01, 0x02), object : SendCallBack {
                        override fun onSending(command:ByteArray?) {
                                Log.i(TAG, "命令发送中...")
                         }

                        override fun onSendFailure(bluetoothException: BluetoothException) {
                                Log.i(TAG, "命令发送失败，bluetoothException:${bluetoothException.message}")
                         }
                         })


 7.那么如何接收蓝牙设备返回给你的数据呢，很简单，在Receiver中接收：

               private fun initListener(){
                   mHBluetooth.setReceiver(object : ReceiveCallBack {
                       override fun onReceived(dataInputStream: DataInputStream?, result: ByteArray?) {
                           // 打开通知后，设备发过来的数据将在这里出现
                           Log.e("mylog", "收到蓝牙设备返回数据->" + bytesToHexString(result))
                       }
                   })
               }


 8.最后，调用以下方法去主动断开连接并释放资源：

                HBluetooth.getInstance().release()




# 更多方法Api介绍：

1.带设备名称过滤条件的扫描：

 fun scan(@BluetoothType scanType: Int, timeUse: Int, filter: ScanFilter?, scanCallBack: ScanCallBack?)

 fun scan(@BluetoothType scanType: Int, filter: ScanFilter?, scanCallBack: ScanCallBack?)


2.BleConfig(BLE)设置分包发送时间间隔(默认20ms)及包长度(默认20个字节)：

 fun splitPacketToSendWhenCmdLenBeyond(splitPacketToSendWhenCmdLenBeyond: Boolean, sendTimeInterval: Int): BleConfig

 fun splitPacketToSendWhenCmdLenBeyond(splitPacketToSendWhenCmdLenBeyond: Boolean, sendTimeInterval: Int, eachSplitPacketLen: Int): BleConfig