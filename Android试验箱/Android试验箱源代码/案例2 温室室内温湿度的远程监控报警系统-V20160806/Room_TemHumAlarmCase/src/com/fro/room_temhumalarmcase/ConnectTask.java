package com.fro.room_temhumalarmcase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.fro.util.FRODigTube;
import com.fro.util.FROTemHum;
import com.fro.util.StreamUtil;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ConnectTask extends AsyncTask<Void, Void, Void> {

	private Context context;
	TextView tem_tv;
	TextView hum_tv;
	TextView info_tv;

	private Float tem;
	private Float hum;
	private byte[] read_buff;

	private Socket temHumSocket;
	private Socket tubeSocket;
	private Socket buzzerSocket;
	private Socket fanSocket;

	private boolean CIRCLE = false;

	public ConnectTask(Context context, TextView data_tv1, TextView data_tv2, TextView info_tv) {
		this.context = context;
		this.tem_tv = data_tv1;
		this.hum_tv = data_tv2;
		this.info_tv = info_tv;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		if (temHumSocket != null && tubeSocket != null && buzzerSocket != null && fanSocket != null) {
			info_tv.setTextColor(context.getResources().getColor(R.color.green));
			info_tv.setText("连接正常！");
		} else {
			info_tv.setTextColor(context.getResources().getColor(R.color.red));
			info_tv.setText("连接失败！");
		}
		if (Const.tem != null && Const.hum != null) {
			tem_tv.setText(String.valueOf(Const.tem));
			hum_tv.setText(String.valueOf(Const.hum));
		}
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
		temHumSocket = getSocket(Const.TEMHUM_IP, Const.TEMHUM_PORT);
		tubeSocket = getSocket(Const.TUBE_IP, Const.TUBE_PORT);
		buzzerSocket = getSocket(Const.BUZZER_IP, Const.BUZZER_PORT);
		fanSocket = getSocket(Const.FAN_IP, Const.FAN_PORT);
		// 循环读取数据
		while (CIRCLE) {
			try {
				// 如果全部连接成功
				if (temHumSocket != null && tubeSocket != null && buzzerSocket != null && fanSocket != null) {

					// 查询光照度
					StreamUtil.writeCommand(temHumSocket.getOutputStream(), Const.TEMHUM_CHK);
					Thread.sleep(Const.time / 2);
					read_buff = StreamUtil.readData(temHumSocket.getInputStream());
					tem = FROTemHum.getTemData(Const.TEMHUM_LEN, Const.TEMHUM_NUM, read_buff);
					hum = FROTemHum.getHumData(Const.TEMHUM_LEN, Const.TEMHUM_NUM, read_buff);
					if (tem != null && hum != null) {
						Const.tem = (int) (float) tem;
						Const.hum = (int) (float) hum;
					}

					// 数码管显示
					Const.TUBE_CMD = FRODigTube.intToCmdString(Const.tem * 100 + Const.hum);
					StreamUtil.writeCommand(tubeSocket.getOutputStream(), Const.TUBE_CMD);
					Thread.sleep(Const.time / 2);

					// 如果联动打开状态并且超过上限，蜂鸣器报警1s，打开风扇
					Log.i(Const.TAG, "Const.linkage=" + Const.linkage);
					Log.i(Const.TAG, "Const.tem=" + Const.tem);
					Log.i(Const.TAG, "Const.hum=" + Const.hum);
					Log.i(Const.TAG, "Const.maxLim=" + Const.maxLim);
					if (Const.linkage && Const.tem > Const.maxLim) {
						// 蜂鸣器
						if (!Const.isBuzzerOn) {
							StreamUtil.writeCommand(buzzerSocket.getOutputStream(), Const.BUZZER_ON);
							Thread.sleep(1000);
							StreamUtil.writeCommand(buzzerSocket.getOutputStream(), Const.BUZZER_OFF);
							Thread.sleep(200);
						}
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
			if (buzzerSocket.getOutputStream() != null) {
				Const.isBuzzerOn = false;
				StreamUtil.writeCommand(buzzerSocket.getOutputStream(), Const.BUZZER_OFF);
				Thread.sleep(200);
				Const.isFanOn = false;
				StreamUtil.writeCommand(fanSocket.getOutputStream(), Const.FAN_OFF);
				Thread.sleep(200);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
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

	void closeSocket() {
		try {
			if (temHumSocket != null) {
				temHumSocket.close();
			}
			if (tubeSocket != null) {
				tubeSocket.close();
			}
			if (buzzerSocket != null) {
				buzzerSocket.close();
			}
			if (fanSocket != null) {
				fanSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
