package com.fro.home_smokealarmcase;

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
import com.fro.util.FROSmoke;
import com.fro.util.StreamUtil;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ConnectTask extends AsyncTask<Void, Void, Void> {

	private Context context;
	TextView smoke_tv;
	TextView info_tv;

	private Float smoke;
	private byte[] read_buff;

	private Socket smokeSocket;
	private Socket tubeSocket;
	private Socket buzzerSocket;
	private Socket fanSocket;

	private boolean CIRCLE = false;

	private boolean isDialogShow = false;

	public ConnectTask(Context context, TextView smoke_tv, TextView info_tv) {
		this.context = context;
		this.smoke_tv = smoke_tv;
		this.info_tv = info_tv;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		if (smokeSocket != null && tubeSocket != null && buzzerSocket != null && fanSocket != null) {
			// if (smokeSocket != null ) {
			info_tv.setTextColor(context.getResources().getColor(R.color.green));
			info_tv.setText("连接正常！");
		} else {
			info_tv.setTextColor(context.getResources().getColor(R.color.red));
			info_tv.setText("连接失败！");
		}

		// 显示数据
		if (Const.smoke != null) {
			smoke_tv.setText(String.valueOf(Const.smoke));
		}

		// 弹框
		if (isDialogShow == false && Const.linkage && Const.smoke != null && Const.smoke > Const.maxLim) {
			// 创建对话框对象
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("系统信息");
			builder.setIcon(android.R.drawable.btn_star);
			final TextView editText = new TextView(context);
			// 添加view进去
			builder.setView(editText);
			editText.setText("烟雾值超过上限了！");
			// 点击确定的响应时间
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					isDialogShow = false;
				}
			});
			isDialogShow = true;
			// 显示对话框
			builder.show();

			// 弹框取消时，捕捉返回事件，让DIALOG_STATU置flase，否则一直不弹框。
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					isDialogShow = false;
					// 消失对话框
					dialog.dismiss();
				}
			});
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
		smokeSocket = getSocket(Const.SMOKE_IP, Const.SMOKE_PORT);
		tubeSocket = getSocket(Const.TUBE_IP, Const.TUBE_PORT);
		buzzerSocket = getSocket(Const.BUZZER_IP, Const.BUZZER_PORT);
		fanSocket = getSocket(Const.FAN_IP, Const.FAN_PORT);
		// 循环读取数据
		while (CIRCLE) {
			try {
				// 如果全部连接成功
				// if (smokeSocket != null ) {
				if (smokeSocket != null && tubeSocket != null && buzzerSocket != null && fanSocket != null) {
					// 查询烟雾值
					StreamUtil.writeCommand(smokeSocket.getOutputStream(), Const.SMOKE_CHK);
					Thread.sleep(Const.time / 2);
					read_buff = StreamUtil.readData(smokeSocket.getInputStream());
					smoke = FROSmoke.getData(Const.SMOKE_LEN, Const.SMOKE_NUM, read_buff);
					if (smoke != null) {
						Const.smoke = (int) (float) smoke;
					}

					// 数码管显示
					if (Const.smoke != null) {
						Const.TUBE_CMD = FRODigTube.intToCmdString(Const.smoke);
						StreamUtil.writeCommand(tubeSocket.getOutputStream(), Const.TUBE_CMD);
						Thread.sleep(Const.time / 2);
					}

					// 如果联动打开状态并且超过上限，蜂鸣器报警1s，打开风机
					Log.i(Const.TAG, "Const.linkage=" + Const.linkage);
					Log.i(Const.TAG, "Const.smoke=" + Const.smoke);
					Log.i(Const.TAG, "Const.maxLim=" + Const.maxLim);
					if (Const.linkage && Const.smoke != null && Const.smoke > Const.maxLim) {
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
			Const.isBuzzerOn = false;
			StreamUtil.writeCommand(buzzerSocket.getOutputStream(), Const.BUZZER_OFF);
			Thread.sleep(200);
			Const.isFanOn = false;
			StreamUtil.writeCommand(fanSocket.getOutputStream(), Const.FAN_OFF);
			Thread.sleep(200);
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
