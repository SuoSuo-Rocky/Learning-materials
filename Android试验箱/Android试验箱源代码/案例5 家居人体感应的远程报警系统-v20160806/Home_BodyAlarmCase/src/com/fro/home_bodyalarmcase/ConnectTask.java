package com.fro.home_bodyalarmcase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.SmsManager;
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

import com.cunoraz.gifview.library.GifView;
import com.fro.util.FROBody;
import com.fro.util.StreamUtil;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ConnectTask extends AsyncTask<Void, Void, Void> {

	private Context context;
	TextView info_tv;
	GifView body_gif;

	private Boolean body;
	private byte[] read_buff;

	private Socket bodySocket;
	private Socket buzzerSocket;

	private boolean CIRCLE = false;

	public ConnectTask(Context context, TextView info_tv, GifView body_gif) {
		this.context = context;
		this.info_tv = info_tv;
		this.body_gif = body_gif;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		if (bodySocket != null && buzzerSocket != null) {
			// if (bodySocket != null ) {
			info_tv.setTextColor(context.getResources().getColor(R.color.green));
			info_tv.setText("连接正常！");
		} else {
			info_tv.setTextColor(context.getResources().getColor(R.color.red));
			info_tv.setText("连接失败！");
		}

		// 感应动画
		if (body_gif.getMovie() == null || body_gif.isPaused()) {
			// 设置当前动态图资源
			body_gif.setMovieResource(R.drawable.gif6);
		}

		// 有人报警
		if (Const.BODY != null) {
			if (Const.BODY) {
				body_gif.setMovieResource(R.drawable.alarm_gif);
				body_gif.setMovieTime(3000);
			}
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
		buzzerSocket = getSocket(Const.BUZZER_IP, Const.BUZZER_PORT);
		// 循环读取数据
		while (CIRCLE) {
			try {
				// 如果全部连接成功
				// if (bodySocket != null ) {
				if (bodySocket != null && buzzerSocket != null) {

					// 查询人体值
					StreamUtil.writeCommand(bodySocket.getOutputStream(), Const.BODY_CHK);
					Thread.sleep(Const.time / 2);
					read_buff = StreamUtil.readData(bodySocket.getInputStream());
					body = FROBody.getData(Const.BODY_LEN, Const.BODY_NUM, read_buff);
					if (body != null) {
						Const.BODY = body;
					}

					// 如果联动打开状态并且有人，蜂鸣器报警,发送短信
					Log.i(Const.TAG, "Const.linkage=" + Const.linkage);
					Log.i(Const.TAG, "Const.body=" + Const.BODY);
					if (Const.linkage && Const.BODY == true) {
						// 触发短信
						Const.SMS = true;
						// 蜂鸣器
						if (!Const.isBuzzerOn) {
							Const.isBuzzerOn = true;
							StreamUtil.writeCommand(buzzerSocket.getOutputStream(), Const.BUZZER_ON);
							Thread.sleep(1000);
							StreamUtil.writeCommand(buzzerSocket.getOutputStream(), Const.BUZZER_OFF);
							Thread.sleep(200);
						}

					} else {
						// 无人，关闭报警
						if (Const.isBuzzerOn) {
							Const.isBuzzerOn = false;
							StreamUtil.writeCommand(buzzerSocket.getOutputStream(), Const.BUZZER_OFF);
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
		// 最后关闭蜂鸣器
		try {
			Const.isBuzzerOn = false;
			StreamUtil.writeCommand(buzzerSocket.getOutputStream(), Const.BUZZER_OFF);
			Thread.sleep(200);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
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
			if (bodySocket != null) {
				bodySocket.close();
			}
			if (buzzerSocket != null) {
				buzzerSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
