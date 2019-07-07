
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fro.ble_smokecase;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

/**
 * 
 * 通过一个 托管在一个给定的蓝牙设备GATT服务器为管理连接和数据通信做服务 
 *
 * 
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class BluetoothLeClass {
	public static final String TAG = MainActivity.TAG;// BluetoothLeClass.class.getSimpleName();

	public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID
			.fromString("00002902-0000-1000-8000-00805f9b34fb");
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;

	public interface OnConnectListener {
		public void onConnect(BluetoothGatt gatt);
	}

	public interface OnDisconnectListener {
		public void onDisconnect(BluetoothGatt gatt);
	}

	public interface OnServiceDiscoverListener {
		public void onServiceDiscover(BluetoothGatt gatt);
	}

	public interface OnDataAvailableListener {
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
	}

	private OnConnectListener mOnConnectListener;
	private OnDisconnectListener mOnDisconnectListener;
	private OnServiceDiscoverListener mOnServiceDiscoverListener;
	private OnDataAvailableListener mOnDataAvailableListener;
	private Context mContext;

	public void setOnConnectListener(OnConnectListener l) {
		mOnConnectListener = l;
	}

	public void setOnDisconnectListener(OnDisconnectListener l) {
		mOnDisconnectListener = l;
	}

	public void setOnServiceDiscoverListener(OnServiceDiscoverListener l) {
		mOnServiceDiscoverListener = l;
	}

	public void setOnDataAvailableListener(OnDataAvailableListener l) {
		mOnDataAvailableListener = l;
	}

	public BluetoothLeClass(Context c) {
		mContext = c;
	}

	/**
	 * 实现回调方法，因为程序需要用到的GATT事件 例如，连接更改和发现服务等会触发到的方法
	 * 
	 * Implements callback methods for GATT events that the app cares about. For
	 * example,connection change and services discovered.
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		/**
		 * 当调用了连接函数 mBluetoothGatt = bluetoothDevice.connectGatt(this.context,
		 * false, gattCallback);或者connect（）之后， 如果连接成功就会 走到 连接状态回调
		 */
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				if (mOnConnectListener != null)
					mOnConnectListener.onConnect(gatt);
				Log.i(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				if (mOnDisconnectListener != null)
					mOnDisconnectListener.onDisconnect(gatt);
				Log.i(TAG, "Disconnected from GATT server.");
			}
		}

		/**
		 * 当判断到连接成功之后，会去寻找服务， 这个过程是异步的，会耗点时间。 当discoverServices()寻找到服务之后，会走到回调：
		 */
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS && mOnServiceDiscoverListener != null) {
				mOnServiceDiscoverListener.onServiceDiscover(gatt);
			} else {
				Log.i(TAG, "onServicesDiscovered received: " + status);
			}
		}

		/**
		 * 
		 * 如果mBluetoothGatt.readCharacteristic() 读取数据（或者读取其他值）成功之后 ，会来到 回调：
		 * 
		 */
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (mOnDataAvailableListener != null)
				mOnDataAvailableListener.onCharacteristicRead(gatt, characteristic, status);
		}

		/**
		 * Notify(通知) notify 就是让设备 可以发送通知给你，也可以说上报值给你（发送命令给你）
		 * 一旦设备那边notify数据给你，你会在回调里收到：
		 */
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			if (mOnDataAvailableListener != null)
				mOnDataAvailableListener.onCharacteristicWrite(gatt, characteristic);
		}
	};

	/**
	 * 初始化一个参考本地蓝牙适配器 Initializes a reference to the local Bluetooth adapter.
	 * 
	 * 如果初始化成功，返回true
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Log.i(TAG, "Unable to initialize Bluetooth,do not has BLE system");
			return false;
		} else {
			Log.i(TAG, "initialize Bluetooth, has BLE system");
		}
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.i(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.i(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		// 直接启动蓝牙
		boolean isEnableBTA = mBluetoothAdapter.enable();
		if (isEnableBTA) {
			Log.i(TAG, "mBluetoothAdapter.enable");
		} else {
			Log.i(TAG, "mBluetoothAdapter.disable");
		}

		return isEnableBTA;
	}

	/**
	 * 连接托管在蓝牙LE设备的GATT服务 Connects to the GATT server hosted on the Bluetooth LE
	 * device.
	 * 
	 * @param address
	 *            目标设备地址 The device address of the destination device.
	 * 
	 * @return 若已连接过的会调用connect(),若未连接过的会调用connectGatt(),如果连接成功，则返回真。
	 *         连接结果会异步通过回调{ @代码bluetoothgattcallback #
	 *         onconnectionstatechange（android.bluetooth.bluetoothgatt，int，int）}
	 *         。 Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.i(TAG, "BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
			Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			Log.i(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
		Log.i(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		return true;
	}

	/**
	 * 断开现有连接或取消挂起的连接。 这个断开的结果会异步通过报告回调 { @代码bluetoothgattcallback #
	 * onconnectionstatechange（android.bluetooth.bluetoothgatt，int，int）}。
	 * 
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.i(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
	}

	/**
	 * 使用一个给定的BLE设备后，应用程序必须调用这个方法来确定资源释放。
	 * 
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * 要求在一个读给定的{ @代码bluetoothgattcharacteristic}。 读结果是通过异步报告回调
	 * { @代码bluetoothgattcallback # oncharacteristicread
	 * （android.bluetooth.bluetoothgatt，android.bluetooth.
	 * bluetoothgattcharacteristic，int）}
	 * 
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead
	 * (android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 * 
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.i(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	/**
	 * 打开或关闭设备的通知功能： 参数 enable 就是打开还是关闭，
	 * characteristic就是你想设置BluetoothGattCharacteristic里的Descriptor，
	 * 让characteristic具有通知功能。
	 * 
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.i(TAG, "BluetoothAdapter not initialized");
			return;
		}
		if (enabled == true) {
			Log.i(TAG, "Enable Notification");
			mBluetoothGatt.setCharacteristicNotification(characteristic, true);
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		} else {
			Log.i(TAG, "Disable Notification");
			mBluetoothGatt.setCharacteristicNotification(characteristic, false);
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
			descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}

		// mBluetoothGatt.setCharacteristicNotification(characteristic,
		// enabled);
	}

	public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * 获得在该已连接的设备的GATT服务列表 这些服务都是在调用{@code BluetoothGatt#discoverServices()}
	 * 后成功的发现的服务。
	 * 
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 * 
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getServices();
	}
	
	/**
	 * getter and setter
	 * @return
	 */

	public BluetoothAdapter getmBluetoothAdapter() {
		return mBluetoothAdapter;
	}

	public void setmBluetoothAdapter(BluetoothAdapter mBluetoothAdapter) {
		this.mBluetoothAdapter = mBluetoothAdapter;
	}

	public BluetoothGatt getmBluetoothGatt() {
		return mBluetoothGatt;
	}

	public void setmBluetoothGatt(BluetoothGatt mBluetoothGatt) {
		this.mBluetoothGatt = mBluetoothGatt;
	}
	
	
}
