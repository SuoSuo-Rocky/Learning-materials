package com.fro.ble_suncase;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.fro.ble_suncase.BluetoothLeClass.OnDataAvailableListener;
import com.fro.ble_suncase.BluetoothLeClass.OnServiceDiscoverListener;
import com.fro.util.FROSun;
import com.fro.util.HexStrConvertUtil;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	public static String TAG = "MainActivity";

	private final Timer timer = new Timer();
	private TimerTask task;
	private Handler handler;

	private static final String UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
	private static final String UUID_CHAR6 = "0000fff6-0000-1000-8000-00805f9b34fb";
	private static final String UUID_DESC = "00002902-0000-1000-8000-00805f9b34fb";

	private ToggleButton scan_tb;
	private ListView bleDev_lv;
	private TextView recv_tv;
	private TextView info_tv;

	// BLE设备
	private BluetoothLeClass mBLE;

	// 蓝牙GATT服务
	private BluetoothGattService mService;

	// 蓝牙GATT数据描述符
	private BluetoothGattCharacteristic gattCharacteristic_char6;

	// 蓝牙BLE列表适配器
	private LeDeviceListAdapter mLeDeviceListAdapter;

	// 接收的字段
	private StringBuilder recv_sb;
	
	// 是否开启读写数据
	private boolean isReadTaskRun;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 绑定控件
		bindView();

		// 初始化数据
		initData();

		// 初始化事件
		initEvent();

		// 创建BLE实例
		mBLE = new BluetoothLeClass(this);
		// 初始化BLE
		if (mBLE.initialize()) {
			info_tv.setText("初始化BLE成功!");
			// Log.i(TAG, "初始化BLE成功");
		} else {
			info_tv.setText("初始化BLE失败!");
			// Log.i(TAG, "初始化BLE失败");
		}

		// 发现BLE终端的Service时回调
		mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
		// 收到BLE终端数据交互的事件
		mBLE.setOnDataAvailableListener(mOnDataAvailable);
	}

	/**
	 * 绑定控件
	 */
	private void bindView() {
		scan_tb = (ToggleButton) findViewById(R.id.scan_tb);
		bleDev_lv = (ListView) findViewById(R.id.bleDev_lv);
		recv_tv = (TextView) findViewById(R.id.recv_tv);
		info_tv = (TextView) findViewById(R.id.info_tv);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {

		// 接收字段
		recv_sb = new StringBuilder("");
		// BLE列表
		mLeDeviceListAdapter = new LeDeviceListAdapter(this);
		bleDev_lv.setAdapter(mLeDeviceListAdapter);

	}

	/**
	 * 初始化事件
	 */
	private void initEvent() {

		// 处理消息
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				// 间隔1秒,收到msg则读取一次数据
				if (msg.what == 1) {
					if (isReadTaskRun) {
						ReadBle();
					}
				}
			}
		};

		// 定时任务
		task = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);
			}
		};
		
		// 开启读取数据线程
		timer.schedule(task, 100, 1000);

		// 扫描
		scan_tb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// 清空列表
					mLeDeviceListAdapter.clear();
					// 开始扫描
					mBLE.getmBluetoothAdapter().startLeScan(mLeScanCallback);
					info_tv.setText("正在扫描BLE终端...");
				} else {
					// 停止扫描
					mBLE.getmBluetoothAdapter().stopLeScan(mLeScanCallback);
					info_tv.setText("已停止扫描BLE终端");
				}
			}
		});

		// Item点击
		bleDev_lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 停止扫描
				scan_tb.setChecked(false);
				mBLE.getmBluetoothAdapter().stopLeScan(mLeScanCallback);
				// 获得device
				final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
				if (device == null)
					return;
				// 点击切换开启任务
				isReadTaskRun = !isReadTaskRun;
				if (isReadTaskRun) {
					// 建立GATT连接
					info_tv.setText("正在连接BLE终端...");
					if (!mBLE.connect(device.getAddress())) {
						info_tv.setText("连接失败！");
					}
				} else {
					mBLE.disconnect();
					info_tv.setText("断开连接！");
				}
			}
		});

	}

	/**
	 * 搜索到设备Device的回调事件
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mLeDeviceListAdapter.addDevice(device);
					mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	/**
	 * 搜索到BLE终端服务的监听事件
	 */
	private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new OnServiceDiscoverListener() {

		@Override
		public void onServiceDiscover(BluetoothGatt gatt) {
			// 寻找到服务
			// 寻找服务之后，我们就可以和设备进行通信，比如下发配置值，获取设备电量什么的

			// 如上面所说，想要和一个学生通信，先知道他的班级（ServiceUUID）和学号（CharacUUID）
			mService = gatt.getService(UUID.fromString(UUID_SERVICE));
			if (mService != null) {
				// UUID_CHAR6是可以跟蓝牙模块串口通信的Characteristic
				gattCharacteristic_char6 = mService.getCharacteristic(UUID.fromString(UUID_CHAR6));
				if (gattCharacteristic_char6 != null) {
					// 设置设备可通知
					mBLE.setCharacteristicNotification(gattCharacteristic_char6, true);
					// 更新接收区界面显示
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							info_tv.setText("连接成功！");
						}
					});

				} else {
					Log.i(TAG, "mCharacteristic = null");
				}
			} else {
				Log.i(TAG, "mService = null");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						info_tv.setText("设置失败！");
					}
				});
			}
		}

	};

	/**
	 * 收到BLE终端数据交互的监听事件
	 */
	private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new OnDataAvailableListener() {
		/**
		 * BLE终端数据被读的事件
		 */
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			// 执行 mBLE.readCharacteristic(gattCharacteristic); 后就会收到数据
			// if(status == BluetoothGatt.GATT_SUCCESS)
			Log.i(TAG, "onCharRead " + gatt.getDevice().getName() + " read " + characteristic.getUuid().toString()
					+ " -> " + HexStrConvertUtil.bytesToHexString(characteristic.getValue()));

		}

		/**
		 * 收到BLE终端写入数据回调
		 */
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			// 收到设备notify值 （设备上报值），根据 characteristic.getUUID()来判断是谁发送值给你，
			// 根据characteristic.getValue()来获取这个值
			Log.i(TAG, "characteristic.getUuid().toString()=" + characteristic.getUuid().toString());
			if (characteristic.getUuid().toString().equals(UUID_CHAR6)) {

				// 接收数据
				Float fData = FROSun.getData(Const.SUN_LEN, Const.NODE_NUM, characteristic.getValue());
				String value = String.valueOf((int) (float) fData + "Lux");

				Log.i(TAG, "onCharWrite=" + gatt.getDevice().getName() + "   value=" + value);

				// 追加到接收字段
				recv_sb.replace(0, recv_sb.length(), value);
				// 更新接收区界面显示
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateDisplaySendInfo();
					}
				});
			}
		}
	};

	/**
	 * 更新显示接收区
	 * 
	 * @param send
	 * @param recv
	 */
	public void updateDisplaySendInfo() {
		recv_tv.setText(recv_sb.toString());
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		timer.cancel();
		mBLE.getmBluetoothAdapter().stopLeScan(mLeScanCallback);
		mBLE.disconnect();
		mBLE.close();
	}

	/**
	 * 读取BLE数据
	 */
	private void ReadBle() {
		// TODO Auto-generated method stub
		if (mBLE.getmBluetoothGatt() != null && gattCharacteristic_char6 != null) {
			// 获取发送命令
			String writeStr = Const.SUN_CMD;
			// 设置数据内容
			gattCharacteristic_char6.setValue(HexStrConvertUtil.hexStringToByte(writeStr));
			// 往蓝牙模块写入数据
			mBLE.getmBluetoothGatt().writeCharacteristic(gattCharacteristic_char6);
			Log.i(TAG, "读取数据...");
		} else {
			Log.i(TAG, "mBLE.getmBluetoothGatt()=null or gattCharacteristic_char6=null");
		}
	}

}