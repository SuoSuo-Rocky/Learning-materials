package com.fro.wifi_rfidcase;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.fro.util.HexStrConvertUtil;
import com.fro.util.StreamUtil;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ControlTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "ControlTask";

	private Context context;
	private TextView infoTv;

	private Boolean statu = false;
	private String cmd;
	private String err;

	private byte[] read_buff;
	private String readStr;

	private InputStream inputStream;
	private OutputStream outputStream;

	public ControlTask(Context context, TextView infoTv, InputStream inputStream, OutputStream outputStream, String cmd,
			String err) {
		this.context = context;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.cmd = cmd;
		this.err = err;
		this.infoTv = infoTv;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		switch (cmd) {
		// 读卡
		case Constant.READ_CARD_CMD:
			if (statu) {
				Constant.CARD_ID = readStr.substring(readStr.length() - 10, readStr.length() - 2);
				infoTv.setText("正常，卡号为" + Constant.CARD_ID);
				infoTv.setTextColor(context.getResources().getColor(R.color.green));
				statu = false;
			} else {
				infoTv.setText("异常");
				infoTv.setTextColor(context.getResources().getColor(R.color.red));
			}
			break;

		// 读块
		case Constant.READ_AREA_CMD:
			if (statu) {
				Constant.AREA_DATA = readStr.substring(readStr.length() - 36, readStr.length() - 2);
				infoTv.setText("正常，数据为" + Constant.AREA_DATA);
				infoTv.setTextColor(context.getResources().getColor(R.color.green));
				statu = false;
			} else {
				infoTv.setText("异常");
				infoTv.setTextColor(context.getResources().getColor(R.color.red));
			}
			break;

		default:
			if (statu) {
				infoTv.setText("正常");
				infoTv.setTextColor(context.getResources().getColor(R.color.green));
				statu = false;
			} else {
				infoTv.setText("异常");
				infoTv.setTextColor(context.getResources().getColor(R.color.red));
			}
			break;
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
		try {
			// 发送命令
			StreamUtil.writeCommand(outputStream, cmd);
			Thread.sleep(200);
			read_buff = StreamUtil.readData(inputStream);
			// 如果设备无返回值，直接返回null
			if(read_buff==null || read_buff.length<10)return null;
			// 读取返回字符串
			readStr = HexStrConvertUtil.bytesToHexString(read_buff);
			Log.i(TAG, "readStr=" + readStr);
			// 判断是否正常
			statu = !(readStr.equals(err.replace(" ", "").toLowerCase()));
			// 更新界面
			publishProgress();
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}

}
