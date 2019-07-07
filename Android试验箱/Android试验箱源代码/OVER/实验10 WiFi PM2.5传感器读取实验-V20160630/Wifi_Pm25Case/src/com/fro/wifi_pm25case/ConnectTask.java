package com.fro.wifi_pm25case;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.fro.util.FROPm25;
import com.fro.util.StreamUtil;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ConnectTask extends AsyncTask<Void, Void, Void> {

	private Context context;
	private Data data;
	private Float pm25;

	private byte[] read_buff;

	private TextView error_tv;
	private TextView smoke_tv;

	private Socket mSocket;
	private SocketAddress mSocketAddress;
	private InputStream inputStream;
	private OutputStream outputStream;

	private Boolean STATU = false;
	private Boolean CIRCLE = false;

	public ConnectTask(Context context, Data data, TextView smoke_tv, TextView error_tv) {
		this.context = context;
		this.data = data;
		this.smoke_tv = smoke_tv;
		this.error_tv = error_tv;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		if (STATU) {
			error_tv.setTextColor(context.getResources().getColor(R.color.green));
			error_tv.setText("连接正常！");
		} else {
			error_tv.setTextColor(context.getResources().getColor(R.color.red));
			error_tv.setText("连接失败！");
		}
			
		smoke_tv.setText(String.valueOf(data.getPm()));
	}
	
	/**
	 * 准备
	 */
	@Override
	protected void onPreExecute() {
		error_tv.setText("正在连接...");
	}

	/**
	 * 子线程任务
	 * 
	 * @param params
	 * @return
	 */
	@Override
	protected Void doInBackground(Void... params) {
		mSocket = new Socket();
		mSocketAddress = new InetSocketAddress(Constant.IP, Constant.port);
		try {
			// socket连接
			mSocket.connect(mSocketAddress, 3000);// 设置连接超时时间为3秒
			if (mSocket.isConnected()) {
				setSTATU(true);
				inputStream = mSocket.getInputStream();// 得到输入流
				outputStream = mSocket.getOutputStream();// 得到输出流
			} else {
				setSTATU(false);
			}

			// 循环读取数据
			while (CIRCLE) {
				// 查询烟雾值
				StreamUtil.writeCommand(outputStream, Constant.PM25_CHK);
				Thread.sleep(200);
				read_buff = StreamUtil.readData(inputStream);
				pm25 = FROPm25.getData(Constant.PM25_LEN, Constant.PM25_NUM, read_buff);
				if (pm25 != null) {
					data.setPm((int)(float)pm25);
				}
				// 更新界面
				publishProgress();
				Thread.sleep(200);
			}

		} catch (IOException e) {
			setSTATU(false);
			publishProgress();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 判断socket是否还在连接
	 * 
	 * @return
	 */
	public Boolean isSuccess() {
		return mSocket.isConnected();
	}

	/**
	 * 获取socket
	 * 
	 * @return
	 */
	public Socket getmSocket() {
		return mSocket;
	}

	/**
	 * 获取输入流
	 * 
	 * @return
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * 获取输出流
	 * 
	 * @return
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	public Boolean getSTATU() {
		return STATU;
	}

	public void setSTATU(Boolean sTATU) {
		STATU = sTATU;
	}

	public Boolean getCIRCLE() {
		return CIRCLE;
	}

	public void setCIRCLE(Boolean cIRCLE) {
		CIRCLE = cIRCLE;
	}

}
