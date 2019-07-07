package com.fro.wifi_fancase;

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

import com.fro.util.FROIOControl;
import com.fro.util.StreamUtil;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ControlTask extends AsyncTask<Void, Void, Void> {

	private Context context;
	private Boolean statu;
	private String cmd;

	private byte[] read_buff;

	private InputStream inputStream;
	private OutputStream outputStream;

	public ControlTask(Context context, InputStream inputStream, OutputStream outputStream, String cmd) {
		this.context = context;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.cmd = cmd;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		if (statu == true) {
			Toast.makeText(context, "操作成功！", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(context, "操作失败！", Toast.LENGTH_SHORT).show();
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
			if(read_buff==null || read_buff.length<11)return null;
			statu = FROIOControl.isSuccess(Constant.FAN_LEN, Constant.FAN_NUM, read_buff);
			// 更新界面
			publishProgress();
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}

}
