package com.fro.wifi_buzzercase;

import android.util.Log;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.AsyncTask;

public class MainActivity extends Activity {

	private static String TAG = "MainActivity";

	private EditText ip_et;
	private EditText port_et;
	private ToggleButton connect_tb;
	private TextView error_tv;

	private Button closeAllBtn;
	private Button buzzerBtn;
	private Button redLightBtn;
	private Button greenLightBtn;
	private Button blueLightBtn;

	private Context context;
	private ConnectTask connectTask;
	private ControlTask controlTask;

	// 当前命令
	private String curCmd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 控件
		bindView();
		// 数据
		initData();
		// 事件
		initEvent();

	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		context = this;
		error_tv.setText("请点击连接！");
		ip_et.setText(Constant.IP);
		port_et.setText(String.valueOf(Constant.port));
	}

	/**
	 * 绑定控件
	 */
	private void bindView() {
		ip_et = (EditText) findViewById(R.id.ip_et);
		port_et = (EditText) findViewById(R.id.port_et);
		connect_tb = (ToggleButton) findViewById(R.id.connect_tb);
		error_tv = (TextView) findViewById(R.id.error_tv);
		closeAllBtn = (Button) findViewById(R.id.closeAllBtn);
		buzzerBtn = (Button) findViewById(R.id.buzzerBtn);
		redLightBtn = (Button) findViewById(R.id.redLightBtn);
		greenLightBtn = (Button) findViewById(R.id.greenLightBtn);
		blueLightBtn = (Button) findViewById(R.id.blueLightBtn);
	}

	/**
	 * 按钮监听
	 */
	private void initEvent() {
		// 连接
		connect_tb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// 获取IP和端口
					String IP = ip_et.getText().toString().trim();
					String port = port_et.getText().toString().trim();
					if(checkIpPort(IP, port)){
						Constant.IP=IP;
						Constant.port=Integer.parseInt(port);
					}else{
						Toast.makeText(context, "IP和端口输入不正确,请重输！", Toast.LENGTH_SHORT).show();
						return;
					}
					// Log.i(TAG, "ip=" + Constant.IP + " port=" +
					// Constant.port);
					// 开启任务
					connectTask = new ConnectTask(context, error_tv);
					connectTask.execute();
				} else {
					// 取消任务
					if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
						// 如果Task还在运行，则先取消它
						connectTask.setSTATU(false);
						connectTask.cancel(true);
					}
					// 关闭socket
					if (ConnectTask.getmSocket() != null) {
						try {
							ConnectTask.getmSocket().close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					error_tv.setText("请点击连接！");
					error_tv.setTextColor(context.getResources().getColor(R.color.gray));
				}
			}
		});

		// 全关
		closeAllBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (connectTask.getSTATU()) {
					controlTask = new ControlTask(context, connectTask.getInputStream(), connectTask.getOutputStream(),
							Constant.CLOSEALL_CMD);
					controlTask.execute();
				} else {
					Toast.makeText(context, "请先连接再操作！", Toast.LENGTH_SHORT).show();
				}
			}
		});

		// 蜂鸣器
		buzzerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (connectTask!=null && connectTask.getSTATU()) {
					controlTask = new ControlTask(context, connectTask.getInputStream(), connectTask.getOutputStream(),
							Constant.BUZZER_CMD);
					controlTask.execute();
				} else {
					Toast.makeText(context, "请先连接再操作！", Toast.LENGTH_SHORT).show();
				}

			}
		});

		// 红灯
		redLightBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (connectTask!=null && connectTask.getSTATU()) {
					controlTask = new ControlTask(context, connectTask.getInputStream(), connectTask.getOutputStream(),
							Constant.RED_CMD);
					controlTask.execute();
				} else {
					Toast.makeText(context, "请先连接再操作！", Toast.LENGTH_SHORT).show();
				}

			}
		});

		// 绿灯
		greenLightBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (connectTask!=null && connectTask.getSTATU()) {
					controlTask = new ControlTask(context, connectTask.getInputStream(), connectTask.getOutputStream(),
							Constant.GREEN_CMD);
					controlTask.execute();
				} else {
					Toast.makeText(context, "请先连接再操作！", Toast.LENGTH_SHORT).show();
				}

			}
		});

		// 蓝灯
		blueLightBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (connectTask!=null && connectTask.getSTATU()) {
					controlTask = new ControlTask(context, connectTask.getInputStream(), connectTask.getOutputStream(),
							Constant.BLUE_CMD);
					controlTask.execute();
				} else {
					Toast.makeText(context, "请先连接再操作！", Toast.LENGTH_SHORT).show();
				}

			}
		});

	}

	/**
	 * IP地址可用端口号验证，可用端口号（1024-65536）
	 * @param IP
	 * @param port
	 * @return
	 */
	private boolean checkIpPort(String IP,String port){
		boolean isIpAddress= false;
		boolean isPort = false;
		
		if(IP==null || IP.length() < 7 || IP.length() > 15 || "".equals(IP) 
				|| port==null || port.length() < 4 || port.length() > 5)
	      {
	        return false;
	      }
	       //判断IP格式和范围
	      String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
	      
	      Pattern pat = Pattern.compile(rexp);  
	      
	      Matcher mat = pat.matcher(IP);  
	      
	      isIpAddress = mat.find();
	      
	      //判断端口
	      int portInt=Integer.parseInt(port);
	      if(portInt>1024 && portInt<65536){
	    	  isPort=true;
	      }

		return (isIpAddress&&isPort);
	}
	
	@Override
	public void finish() {
		super.finish();
		try {
			// 取消任务
			if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
				// 如果Task还在运行，则先取消它
				connectTask.cancel(true);
				connectTask.getmSocket().close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
