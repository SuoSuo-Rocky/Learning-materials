package com.fro.ble_buzzercase;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LeDeviceListAdapter extends BaseAdapter {
	private ArrayList<BluetoothDevice> mLeDevices;
	private LayoutInflater mInflator;
	private Activity mContext;

	public LeDeviceListAdapter(Activity c) {
		super();
		mContext = c;
		mLeDevices = new ArrayList<BluetoothDevice>();
		mInflator = mContext.getLayoutInflater();
	}

	// 添加设备进列表中
	public void addDevice(BluetoothDevice device) {
		if (!mLeDevices.contains(device)) {
			mLeDevices.add(device);
		}
	}

	// 获取子项中对应的设备
	public BluetoothDevice getDevice(int position) {
		return mLeDevices.get(position);
	}

	// 清空列表的数据
	public void clear() {
		mLeDevices.clear();
	}

	@Override
	public int getCount() {
		return mLeDevices.size();
	}

	@Override
	public Object getItem(int i) {
		return mLeDevices.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		// General ListView optimization code.
		if (view == null) {
			view = mInflator.inflate(R.layout.listitem_device, null);
			viewHolder = new ViewHolder();
			viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
			viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		// 对应的设备进行处理
		BluetoothDevice device = mLeDevices.get(i);
		final String deviceName = device.getName();
		if (deviceName != null && deviceName.length() > 0) {
			viewHolder.deviceName.setText(deviceName);
		} else {
			viewHolder.deviceName.setText("未知设备");
		}
		viewHolder.deviceAddress.setText(device.getAddress());

		return view;
	}

	final class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}
}
