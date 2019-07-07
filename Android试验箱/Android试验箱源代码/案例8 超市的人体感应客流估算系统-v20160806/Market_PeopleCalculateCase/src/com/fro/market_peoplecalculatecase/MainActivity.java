package com.fro.market_peoplecalculatecase;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	
	private Context context;

	private EditText bodyIp_et;
	private EditText bodyPort_et;
	private EditText tubeIp_et;
	private EditText tubePort_et;
	
	private TextView body_tv;
	private ToggleButton connect_tb;
	private TextView info_tv;
	
	private ConnectTask connectTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context=this;

		// 绑定控件
		bindView();
		// 初始化数据
		initData();
		// 事件监听
		initEvent();
	}
	
	/**
	 * 绑定控件
	 */
	private void bindView() {
		bodyIp_et = (EditText) findViewById(R.id.bodyIp_et);
		bodyPort_et = (EditText) findViewById(R.id.bodyPort_et);
		tubeIp_et = (EditText) findViewById(R.id.tubeIp_et);
		tubePort_et = (EditText) findViewById(R.id.tubePort_et);
		
		connect_tb = (ToggleButton) findViewById(R.id.connect_tb);
		info_tv = (TextView) findViewById(R.id.info_tv);
		body_tv = (TextView) findViewById(R.id.body_tv);
	}
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		bodyIp_et.setText(Const.BODY_IP);
		bodyPort_et.setText(String.valueOf(Const.BODY_PORT));
		tubeIp_et.setText(Const.TUBE_IP);
		tubePort_et.setText(String.valueOf(Const.TUBE_PORT));
		info_tv.setText("请点击连接！");
	}

	/**
	 * 按钮监听
	 */
	private void initEvent() {
		
		//连接
		connect_tb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// 获取IP和端口
                    String BODY_IP = bodyIp_et.getText().toString().trim();
                    String BODY_PORT = bodyPort_et.getText().toString().trim();
                    String TUBE_IP = tubeIp_et.getText().toString().trim();
                    String TUBE_PORT = tubePort_et.getText().toString().trim();
                    if(checkIpPort(BODY_IP, BODY_PORT) &&checkIpPort(TUBE_IP, TUBE_PORT)){
                    	Const.BODY_IP=BODY_IP;
                    	Const.BODY_PORT=Integer.parseInt(BODY_PORT);
                    	Const.TUBE_IP=TUBE_IP;
                    	Const.TUBE_PORT=Integer.parseInt(TUBE_PORT);
                    }else{
                        Toast.makeText(context, "配置信息不正确,请重输！", Toast.LENGTH_SHORT).show();
                        return;
                    }
					// 开启任务
					connectTask = new ConnectTask(context, body_tv, info_tv);
					connectTask.setCIRCLE(true);
					connectTask.execute();
				} else {
					// 取消任务
					if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
						connectTask.setCIRCLE(false);
						// 如果Task还在运行，则先取消它
						connectTask.cancel(true);
						connectTask.closeSocket();
					}
					info_tv.setText("请点击连接！");
					info_tv.setTextColor(context.getResources().getColor(R.color.gray));
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
			// 取消任务
			if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
				connectTask.setCIRCLE(false);
				// 如果Task还在运行，则先取消它
				connectTask.cancel(true);
				connectTask.closeSocket();
			}
	}
}
