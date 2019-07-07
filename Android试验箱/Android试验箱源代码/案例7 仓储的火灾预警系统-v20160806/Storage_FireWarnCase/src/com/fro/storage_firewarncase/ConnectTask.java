package com.fro.storage_firewarncase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.fro.util.FROSmoke;
import com.fro.util.FROTemHum;
import com.fro.util.StreamUtil;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ConnectTask extends AsyncTask<Void, Void, Void> {

	private Context context;
	TextView tem_tv;
	TextView hum_tv;
	TextView smoke_tv;
	TextView warnCount_tv;
	TextView info_tv;

	private Float tem;
	private Float hum;
	private Float smoke;
	private byte[] read_buff;

	private Socket smokeSocket;
	private Socket temHumSocket;
	private Socket fanSocket;

	private boolean CIRCLE = false;

	private boolean isDialogShow = false;

	public ConnectTask(Context context, TextView tem_tv, TextView hum_tv, TextView smoke_tv, TextView warnCount_tv,
			TextView info_tv) {
		this.context = context;
		this.tem_tv = tem_tv;
		this.hum_tv = hum_tv;
		this.smoke_tv = smoke_tv;
		this.warnCount_tv = warnCount_tv;
		this.info_tv = info_tv;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		if (smokeSocket != null && temHumSocket != null && fanSocket != null) {
			// if (smokeSocket != null ) {
			info_tv.setTextColor(context.getResources().getColor(R.color.green));
			info_tv.setText("连接正常！");
		} else {
			info_tv.setTextColor(context.getResources().getColor(R.color.red));
			info_tv.setText("连接失败！");
		}

		// 显示数据
		if (Const.tem != null) {
			tem_tv.setText(String.valueOf(Const.tem));
		}
		if (Const.hum != null) {
			hum_tv.setText(String.valueOf(Const.hum));
		}
		if (Const.smoke != null) {
			smoke_tv.setText(String.valueOf(Const.smoke));
		}

		// 预警次数
		warnCount_tv.setText(String.valueOf(Const.warnCount));
	}

	/**
	 * 准备
	 */
	@Override
	protected void onPreExecute() {
		info_tv.setText("正在连接...");
	}

	/**
	 * 子线程任务
	 * 
	 * @param params
	 * @return
	 */
	@Override
	protected Void doInBackground(Void... params) {
		// 连接
		smokeSocket = getSocket(Const.SMOKE_IP, Const.SMOKE_PORT);
		temHumSocket = getSocket(Const.TEMHUM_IP, Const.TEMHUM_PORT);
		fanSocket = getSocket(Const.FAN_IP, Const.FAN_PORT);
		// 循环读取数据
		while (CIRCLE) {
			try {
				// 如果全部连接成功
				// if (smokeSocket != null ) {
				if (smokeSocket != null && temHumSocket != null && fanSocket != null) {

					// 查询温湿度值
					StreamUtil.writeCommand(temHumSocket.getOutputStream(), Const.TEMHUM_CHK);
					Thread.sleep(Const.time / 2);
					read_buff = StreamUtil.readData(temHumSocket.getInputStream());
					tem = FROTemHum.getTemData(Const.TEMHUM_LEN, Const.TEMHUM_NUM, read_buff);
					if (tem != null) {
						Const.tem = (int) (float) tem;
					}
					hum = FROTemHum.getHumData(Const.TEMHUM_LEN, Const.TEMHUM_NUM, read_buff);
					if (hum != null) {
						Const.hum = (int) (float) hum;
					}

					// 查询烟雾值
					StreamUtil.writeCommand(smokeSocket.getOutputStream(), Const.SMOKE_CHK);
					Thread.sleep(Const.time / 2);
					read_buff = StreamUtil.readData(smokeSocket.getInputStream());
					smoke = FROSmoke.getData(Const.SMOKE_LEN, Const.SMOKE_NUM, read_buff);
					if (smoke != null) {
						Const.smoke = (int) (float) smoke;
					}

					// 如果联动打开状态并且温度>上限,湿度<下限,烟雾>上限，打开风机
					Log.i(Const.TAG, "Const.linkage=" + Const.linkage);
					Log.i(Const.TAG, "Const.tem=" + Const.tem);
					Log.i(Const.TAG, "Const.temMaxLim=" + Const.temMaxLim);
					Log.i(Const.TAG, "Const.hum=" + Const.hum);
					Log.i(Const.TAG, "Const.humMinLim=" + Const.humMinLim);
					Log.i(Const.TAG, "Const.smoke=" + Const.smoke);
					Log.i(Const.TAG, "Const.smokeMaxLim=" + Const.smokeMaxLim);
					if (Const.linkage && Const.tem > Const.temMaxLim && Const.hum < Const.humMinLim
							&& Const.smoke > Const.smokeMaxLim) {
						// 预警次数加1
						Const.warnCount++;
						// 风扇
						if (!Const.isFanOn) {
							Const.isFanOn = true;
							StreamUtil.writeCommand(fanSocket.getOutputStream(), Const.FAN_ON);
							Thread.sleep(200);
						}
					} else {
						if (Const.isFanOn) {
							Const.isFanOn = false;
							StreamUtil.writeCommand(fanSocket.getOutputStream(), Const.FAN_OFF);
							Thread.sleep(200);
						}
					}
				}
				// 更新界面
				publishProgress();
				Thread.sleep(200);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 最后关闭蜂鸣器，关闭风扇
		try {
			if(fanSocket!=null){
			Const.isFanOn = false;
			StreamUtil.writeCommand(fanSocket.getOutputStream(), Const.FAN_OFF);
			Thread.sleep(200);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			closeSocket();
		}
		return null;
	}

	/**
	 * 建立连接并返回socket，若连接失败返回null
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	private Socket getSocket(String ip, int port) {
		Socket mSocket = new Socket();
		InetSocketAddress mSocketAddress = new InetSocketAddress(ip, port);
		// socket连接
		try {
			// 设置连接超时时间为3秒
			mSocket.connect(mSocketAddress, 3000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 检查是否连接成功
		if (mSocket.isConnected()) {
			Log.i(Const.TAG, ip + "连接成功！");
			return mSocket;
		} else {
			Log.i(Const.TAG, ip + "连接失败！");
			return null;
		}
	}

	public void setCIRCLE(boolean cIRCLE) {
		CIRCLE = cIRCLE;
	}

	@Override
	protected void onCancelled() {
		info_tv.setTextColor(context.getResources().getColor(R.color.gray));
		info_tv.setText("请点击连接！");
	}

	/**
	 * 关闭socket
	 */
	void closeSocket() {
		try {
			if (smokeSocket != null) {
				smokeSocket.close();
			}
			if (temHumSocket != null) {
				temHumSocket.close();
			}
			if (fanSocket != null) {
				fanSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
