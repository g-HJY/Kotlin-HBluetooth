# HBluetooth
封装了支持蓝牙2.0和4.0扫描，连接，以及通信的库。附带使用例子。该库后续会持续升级维护，敬请关注...

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
	     implementation 'com.github.g-HJY:HBluetooth:1.0.1'
	}
 

二.使用介绍

1.第一步，使用前先实例化HBluetooth（全局单例）,并且必须调用enableBluetooth()方法开启蓝牙功能：
 
               HBluetooth.getInstance(this).enableBluetooth()
 
 
 
 2.开启蓝牙能力后，接着你就可以开始进行蓝牙设备扫描，其中，type 为蓝牙设备类型（蓝牙2.0或4.0）：

               HBluetooth.getInstance(this)
                    .scanner()
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
            HBluetooth.getInstance(this)
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
            
            
            
3.一旦扫描到设备，你就可以找到目标设备并连接：
            
            HBluetooth.getInstance(this)
                .connector()?
                .connect(device, object : ConnectCallBack {
                    override fun onConnecting() {
                        Log.i(TAG, "连接中...")
                    }

                    override fun onConnected(sender: Sender?) {
                        Log.i(TAG, "连接成功,isConnected:" + mHBluetooth!!.isConnected)
                        //调用发送器发送命令
                        sender?.send(byteArrayOf(0x01, 0x02), object : SendCallBack {
                            override fun onSending() {
                                Log.i(TAG, "命令发送中...")
                            }

                            override fun onReceived(dataInputStream: DataInputStream?, bleValue: ByteArray) {
                                Log.i(TAG, "onReceived->$dataInputStream---$bleValue")
                            }
                        })
                    }

                    override fun onDisConnecting() {
                        Log.i(TAG, "断开连接中...")
                    }

                    override fun onDisConnected() {
                        Log.i(TAG, "已断开连接,isConnected:" + mHBluetooth!!.isConnected)
                    }

                    override fun onError(errorType: Int, errorMsg: String) {
                        Log.i(TAG, "错误类型：$errorType 错误原因：$errorMsg")
                    }
                })
                
                
 4.设备连接成功后，你可以开始跟设备进行通信：
               
               HBluetooth.getInstance(this)
                                .sender()?
                                .send(byteArrayOf(0x01, 0x02), object : SendCallBack {
                            override fun onSending() {
                                Log.i(TAG, "命令发送中...")
                            }

                            override fun onReceived(dataInputStream: DataInputStream?, bleValue: ByteArray) {
                                Log.i(TAG, "onReceived->$dataInputStream---$bleValue")
                            }
                        })
                        
 5.最后，调用以下方法去主动断开连接并释放资源 ：
                
                HBluetooth.getInstance(this).release();
                
