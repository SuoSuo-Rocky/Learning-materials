package com.fro.home_smokealarmcase;

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

	private EditText smokeIp_et;
	private EditText smokePort_et;
	private EditText tubeIp_et;
	private EditText tubePort_et;
	private EditText buzzerIp_et;
	private EditText buzzerPort_et;
	private EditText fanIp_et;
	private EditText fanPort_et;

	private EditText time_et;
	private TextView smoke_tv;
	private EditText maxLim_et;
	private ToggleButton connect_tb;
	private TextView info_tv;

	private ConnectTask connectTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;

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
		smokeIp_et = (EditText) findViewById(R.id.smokeIp_et);
		smokePort_et = (EditText) findViewById(R.id.smokePort_et);
		tubeIp_et = (EditText) findViewById(R.id.tubeIp_et);
		tubePort_et = (EditText) findViewById(R.id.tubePort_et);
		buzzerIp_et = (EditText) findViewById(R.id.buzzerIp_et);
		buzzerPort_et = (EditText) findViewById(R.id.buzzerPort_et);
		fanIp_et = (EditText) findViewById(R.id.fanIp_et);
		fanPort_et = (EditText) findViewById(R.id.fanPort_et);

		time_et = (EditText) findViewById(R.id.time_et);
		maxLim_et = (EditText) findViewById(R.id.maxLim_et);
		connect_tb = (ToggleButton) findViewById(R.id.connect_tb);
		info_tv = (TextView) findViewById(R.id.info_tv);
		smoke_tv = (TextView) findViewById(R.id.smoke_tv);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		smokeIp_et.setText(Const.SMOKE_IP);
		smokePort_et.setText(String.valueOf(Const.SMOKE_PORT));
		tubeIp_et.setText(Const.TUBE_IP);
		tubePort_et.setText(String.valueOf(Const.TUBE_PORT));
		buzzerIp_et.setText(Const.BUZZER_IP);
		buzzerPort_et.setText(String.valueOf(Const.BUZZER_PORT));
		fanIp_et.setText(Const.FAN_IP);
		fanPort_et.setText(String.valueOf(Const.FAN_PORT));

		time_et.setText(String.valueOf(Const.time));
		maxLim_et.setText(String.valueOf(Const.maxLim));

		info_tv.setText("请点击连接！");
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
                    String SMOKE_IP = smokeIp_et.getText().toString().trim();
                    String SMOKE_PORT = smokePort_et.getText().toString().trim();
                    String TUBE_IP = tubeIp_et.getText().toString().trim();
                    String TUBE_PORT = tubePort_et.getText().toString().trim();
                    String BUZZER_IP = buzzerIp_et.getText().toString().trim();
                    String BUZZER_PORT = buzzerPort_et.getText().toString().trim();
                    String FAN_IP = fanIp_et.getText().toString().trim();
                    String FAN_PORT = fanPort_et.getText().toString().trim();
                    if(checkIpPort(SMOKE_IP, SMOKE_PORT) &&checkIpPort(TUBE_IP, TUBE_PORT)
                    		&&checkIpPort(BUZZER_IP, BUZZER_PORT) &&checkIpPort(FAN_IP, FAN_PORT) ){
                    	Const.SMOKE_IP=SMOKE_IP;
                    	Const.SMOKE_PORT=Integer.parseInt(SMOKE_PORT);
                    	Const.TUBE_IP=TUBE_IP;
                    	Const.TUBE_PORT=Integer.parseInt(TUBE_PORT);
                    	Const.BUZZER_IP=BUZZER_IP;
                    	Const.BUZZER_PORT=Integer.parseInt(BUZZER_PORT);
                    	Const.FAN_IP=FAN_IP;
                    	Const.FAN_PORT=Integer.parseInt(FAN_PORT);
                    }else{
                        Toast.makeText(context, "配置信息不正确,请重输！", Toast.LENGTH_SHORT).show();
                        return;
                    }
					// 获取其他参数
					Const.time = Integer.parseInt(time_et.getText().toString().trim());
					Const.maxLim = Integer.parseInt(maxLim_et.getText().toString().trim());
					// 开启任务
					connectTask = new ConnectTask(context, smoke_tv, info_tv);
					connectTask.setCIRCLE(true);
					connectTask.execute();
				} else {
					// 取消任务
					if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
						connectTask.setCIRCLE(false);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 如果Task还在运行，则先取消它
						connectTask.cancel(true);
						connectTask.closeSocket();
					}
					Const.smoke=null;
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
