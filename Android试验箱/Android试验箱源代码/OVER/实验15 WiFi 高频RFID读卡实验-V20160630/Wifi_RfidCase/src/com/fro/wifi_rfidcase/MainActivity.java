package com.fro.wifi_rfidcase;

import android.util.Log;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fro.util.CRCValidate;
import com.fro.util.HexStrConvertUtil;

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

	private Button findCardBtn;
	private TextView findCardTv;

	private Button readCardBtn;
	private TextView readCardTv;

	private Button chooseCardBtn;
	private TextView chooseCardTv;

	private Button chooseAreaBtn;
	private TextView chooseAreaTv;

	private Button readAreaBtn;
	private TextView readAreaTv;

	private Context context;
	private ConnectTask connectTask;
	private ControlTask controlTask;

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

		findCardBtn = (Button) findViewById(R.id.findCardBtn);
		findCardTv = (TextView) findViewById(R.id.findCardTv);

		readCardBtn = (Button) findViewById(R.id.readCardBtn);
		readCardTv = (TextView) findViewById(R.id.readCardTv);

		chooseCardBtn = (Button) findViewById(R.id.chooseCardBtn);
		chooseCardTv = (TextView) findViewById(R.id.chooseCardTv);

		chooseAreaBtn = (Button) findViewById(R.id.chooseAreaBtn);
		chooseAreaTv = (TextView) findViewById(R.id.chooseAreaTv);

		readAreaBtn = (Button) findViewById(R.id.readAreaBtn);
		readAreaTv = (TextView) findViewById(R.id.readAreaTv);
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

		// 寻卡
		findCardBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (connectTask!=null && connectTask.getSTATU()) {
					controlTask = new ControlTask(context, findCardTv, connectTask.getInputStream(),
							connectTask.getOutputStream(), Constant.FIND_CARD_CMD, Constant.FIND_CARD_ERROR);
					controlTask.execute();
				} else {
					Toast.makeText(context, "请先连接再操作！", Toast.LENGTH_SHORT).show();
				}

			}
		});

		// 读卡
		readCardBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (connectTask!=null && connectTask.getSTATU()) {
					controlTask = new ControlTask(context, readCardTv, connectTask.getInputStream(),
							connectTask.getOutputStream(), Constant.READ_CARD_CMD, Constant.READ_CARD_ERROR);
					controlTask.execute();
				} else {
					Toast.makeText(context, "请先连接再操作！", Toast.LENGTH_SHORT).show();
				}

			}
		});

		// 选卡
		chooseCardBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (connectTask!=null && connectTask.getSTATU()) {
					//获取读到的卡号
					//"5AEFC604"->"5A EF C6 04";
					Constant.CARD_ID=HexStrConvertUtil.transSpace(Constant.CARD_ID).trim();
					//Log.i(TAG, "Constant.CARD_ID="+Constant.CARD_ID);
					
					//"5A EF C6 04"->"00 00 03 02 5A EF C6 04"
					String cmdNoCrc="00 00 03 02 "+Constant.CARD_ID;
					//Log.i(TAG, "cmdNoCrc="+cmdNoCrc);
					
					//crc={0,0x76}
					byte[] crc={0,(byte)CRCValidate
							.calculateSingleCRC(HexStrConvertUtil.hexStringToByte(cmdNoCrc), 0, cmdNoCrc.length())};
					//{0,0x76}->"00 76"
					String crcStr=" "+HexStrConvertUtil.bytesToHexString(crc).substring(2);
					//Log.i(TAG, "crcStr="+crcStr);
					
					//CHOOSE_CARD_CMD="AA BB 09 00 "+"00 00 03 02 5A EF C6 04"+" 76"
					Constant.CHOOSE_CARD_CMD="AA BB 09 00 "+cmdNoCrc+crcStr;
					//Log.i(TAG, "Constant.CHOOSE_CARD_CMD="+Constant.CHOOSE_CARD_CMD);
					
					controlTask = new ControlTask(context, chooseCardTv, connectTask.getInputStream(),
							connectTask.getOutputStream(), Constant.CHOOSE_CARD_CMD, Constant.CHOOSE_CARD_ERROR);
					controlTask.execute();
				} else {
					Toast.makeText(context, "请先连接再操作！", Toast.LENGTH_SHORT).show();
				}

			}
		});

		// 选块
		chooseAreaBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (connectTask!=null && connectTask.getSTATU()) {
					controlTask = new ControlTask(context, chooseAreaTv, connectTask.getInputStream(),
							connectTask.getOutputStream(), Constant.CHOOSE_AREA_CMD, Constant.CHOOSE_AREA_ERROR);
					controlTask.execute();
				} else {
					Toast.makeText(context, "请先连接再操作！", Toast.LENGTH_SHORT).show();
				}

			}
		});

		// 读块
		readAreaBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (connectTask!=null && connectTask.getSTATU()) {
					controlTask = new ControlTask(context, readAreaTv, connectTask.getInputStream(),
							connectTask.getOutputStream(), Constant.READ_AREA_CMD, Constant.READ_AREA_ERROR);
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
