package com.fro.home_autodoorcase;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.dd.CircularProgressButton;
import com.fro.util.FRORfid;
import com.fro.util.StreamUtil;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ConnectTask extends AsyncTask<Void, Void, Void> {

	private Context context;
	TextView readResult_tv;
	TextView cardId_tv;
	CircularProgressButton connectCircularButton;

	private String cardId;
	private byte[] read_buff;

	private Socket rfidSocket;
	private Socket fanSocket;

	private boolean CIRCLE = false;

	public ConnectTask(Context context, TextView readResult_tv, TextView cardId_tv,
			CircularProgressButton connectCircularButton) {
		this.context = context;
		this.readResult_tv = readResult_tv;
		this.cardId_tv = cardId_tv;
		this.connectCircularButton = connectCircularButton;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		if (rfidSocket != null && fanSocket != null) {

			// 设置当前进度值，100代表完成
			connectCircularButton.setProgress(100);

		} else {
			Log.i(Const.TAG, "连接失败!");
			// 设置当前进度，-1代表失败
			connectCircularButton.setProgress(-1);
		}

		// 显示数据
		if (Const.CARD_ID != null) {
			readResult_tv.setText("读卡成功！");
			cardId_tv.setText(Const.CARD_ID);
		}

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
		rfidSocket = getSocket(Const.RFID_IP, Const.RFID_PORT);
		fanSocket = getSocket(Const.FAN_IP, Const.FAN_PORT);

		// 循环读取数据
		while (CIRCLE) {
			try {
				// 如果全部连接成功
				// if (rfidSocket != null ) {
				if (rfidSocket != null && fanSocket != null) {

					// 寻卡
					StreamUtil.writeCommand(rfidSocket.getOutputStream(), Const.RFID_FIND);
					Thread.sleep(Const.time / 2);
					read_buff = StreamUtil.readData(rfidSocket.getInputStream());
					boolean hasRfid = FRORfid.isFound(read_buff);
					if (hasRfid == true) {

						// 寻卡成功后才读卡号
						StreamUtil.writeCommand(rfidSocket.getOutputStream(), Const.RFID_READ);
						Thread.sleep(Const.time / 2);
						read_buff = StreamUtil.readData(rfidSocket.getInputStream());
						cardId = FRORfid.getCardId(read_buff);
						if (cardId != null) {
							Const.CARD_ID = cardId;

							// 如果能读出卡号验证成功则开门
							Log.i(Const.TAG, "Const.linkage=" + Const.linkage);
							Log.i(Const.TAG, "Const.rfid=" + Const.CARD_ID);
							if (Const.linkage) {
								// 门禁电机
								if (!Const.isFanOn) {
									StreamUtil.writeCommand(fanSocket.getOutputStream(), Const.FAN_ON);
									Thread.sleep(3000);
									StreamUtil.writeCommand(fanSocket.getOutputStream(), Const.FAN_OFF);
									Thread.sleep(200);
								}
							}
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
			Const.isFanOn = false;
			StreamUtil.writeCommand(fanSocket.getOutputStream(), Const.FAN_OFF);
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
		connectCircularButton.setProgress(0);
	}

	/**
	 * 关闭socket
	 */
	void closeSocket() {
		try {
			if (rfidSocket != null) {
				rfidSocket.close();
			}
			if (fanSocket != null) {
				fanSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
