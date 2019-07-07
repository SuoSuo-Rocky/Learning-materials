package com.fro.market_peoplecalculatecase;

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

import com.fro.util.FRODigTube;
import com.fro.util.FROBody;
import com.fro.util.StreamUtil;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ConnectTask extends AsyncTask<Void, Void, Void> {

	private Context context;
	TextView body_tv;
	TextView info_tv;

	private Boolean body;
	private byte[] read_buff;

	private Socket bodySocket;
	private Socket tubeSocket;

	private boolean CIRCLE = false;

	private boolean isDialogShow = false;

	public ConnectTask(Context context, TextView body_tv, TextView info_tv) {
		this.context = context;
		this.body_tv = body_tv;
		this.info_tv = info_tv;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		if (bodySocket != null && tubeSocket != null) {
			// if (bodySocket != null ) {
			info_tv.setTextColor(context.getResources().getColor(R.color.green));
			info_tv.setText("连接正常！");
		} else {
			info_tv.setTextColor(context.getResources().getColor(R.color.red));
			info_tv.setText("连接失败！");
		}

		// 显示数据
		if (Const.body != null) {
			body_tv.setText(String.valueOf(Const.body));
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
		bodySocket = getSocket(Const.BODY_IP, Const.BODY_PORT);
		tubeSocket = getSocket(Const.TUBE_IP, Const.TUBE_PORT);
		// 循环读取数据
		while (CIRCLE) {
			try {
				// 如果全部连接成功
				// if (bodySocket != null ) {
				if (bodySocket != null && tubeSocket != null) {
					// 估计客流值(不停监测，一旦检测到有人则累加1)
					StreamUtil.writeCommand(bodySocket.getOutputStream(), Const.BODY_CHK);
					Thread.sleep(Const.time);
					read_buff = StreamUtil.readData(bodySocket.getInputStream());
					body = FROBody.getData(Const.BODY_LEN, Const.BODY_NUM, read_buff);
					if (body != null && body) {
						Const.body++;
					}

					// 数码管显示
					Const.TUBE_CMD = FRODigTube.intToCmdString(Const.body);
					StreamUtil.writeCommand(tubeSocket.getOutputStream(), Const.TUBE_CMD);
					Thread.sleep(200);

					// 输出客流数
					Log.i(Const.TAG, "Const.body=" + Const.body);
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
			if (bodySocket != null) {
				bodySocket.close();
			}
			if (tubeSocket != null) {
				tubeSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
