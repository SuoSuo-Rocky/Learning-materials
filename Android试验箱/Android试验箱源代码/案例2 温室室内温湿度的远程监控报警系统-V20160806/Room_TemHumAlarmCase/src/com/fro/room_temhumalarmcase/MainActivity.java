package com.fro.room_temhumalarmcase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private Context context;

	private EditText temHumIp_et;
	private EditText temHumPort_et;
	private EditText tubeIp_et;
	private EditText tubePort_et;
	private EditText buzzerIp_et;
	private EditText buzzerPort_et;
	private EditText fanIp_et;
	private EditText fanPort_et;

	private EditText time_et;
	private TextView tem_tv;
	private TextView hum_tv;
	private NumberPicker maxLim_et;
	private ToggleButton connect_tb;
	private TextView info_tv;
	private ToggleButton linkage_sw;

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
		temHumIp_et = (EditText) findViewById(R.id.temHumIp_et);
		temHumPort_et = (EditText) findViewById(R.id.temHumPort_et);
		tubeIp_et = (EditText) findViewById(R.id.tubeIp_et);
		tubePort_et = (EditText) findViewById(R.id.tubePort_et);
		buzzerIp_et = (EditText) findViewById(R.id.buzzerIp_et);
		buzzerPort_et = (EditText) findViewById(R.id.buzzerPort_et);
		fanIp_et = (EditText) findViewById(R.id.fanIp_et);
		fanPort_et = (EditText) findViewById(R.id.fanPort_et);

		tem_tv = (TextView) findViewById(R.id.tem_tv);
		hum_tv = (TextView) findViewById(R.id.hum_tv);

		time_et = (EditText) findViewById(R.id.time_et);
		maxLim_et = (NumberPicker) findViewById(R.id.maxLim_et);
		connect_tb = (ToggleButton) findViewById(R.id.connect_tb);
		info_tv = (TextView) findViewById(R.id.info_tv);
		linkage_sw = (ToggleButton) findViewById(R.id.linkage_sw);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		temHumIp_et.setText(Const.TEMHUM_IP);
		temHumPort_et.setText(String.valueOf(Const.TEMHUM_PORT));
		tubeIp_et.setText(Const.TUBE_IP);
		tubePort_et.setText(String.valueOf(Const.TUBE_PORT));
		buzzerIp_et.setText(Const.BUZZER_IP);
		buzzerPort_et.setText(String.valueOf(Const.BUZZER_PORT));
		fanIp_et.setText(Const.FAN_IP);
		fanPort_et.setText(String.valueOf(Const.FAN_PORT));

		info_tv.setText("请点击连接!");

		// maxLim_et.setText(String.valueOf(Const.maxLim));

		// 设置最大滚动值
		maxLim_et.setMaxValue(41);// 注意0~40，一共41个数
		// 设置最小滚动值
		maxLim_et.setMinValue(0);
		// 设置当前选择值
		maxLim_et.setValue(Const.maxLim);

		time_et.setText(String.valueOf(Const.time));

		// 设置开状态
		linkage_sw.setChecked(true);
	}

	/**
	 * 按钮监听
	 */
	private void initEvent() {
		// 联动
		linkage_sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Const.linkage = isChecked;
			}
		});

		// 连接
		connect_tb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// 获取IP和端口
					String TEMHUM_IP = temHumIp_et.getText().toString().trim();
					String TEMHUM_PORT = temHumPort_et.getText().toString().trim();
					String TUBE_IP = tubeIp_et.getText().toString().trim();
					String TUBE_PORT = tubePort_et.getText().toString().trim();
					String BUZZER_IP = buzzerIp_et.getText().toString().trim();
					String BUZZER_PORT = buzzerPort_et.getText().toString().trim();
					String FAN_IP = fanIp_et.getText().toString().trim();
					String FAN_PORT = fanPort_et.getText().toString().trim();
					// 获取其他参数
					String time = time_et.getText().toString().trim();
					if (checkIpPort(TEMHUM_IP, TEMHUM_PORT) && checkIpPort(TUBE_IP, TUBE_PORT)
							&& checkIpPort(BUZZER_IP, BUZZER_PORT) && checkIpPort(FAN_IP, FAN_PORT) && time != null
							&& time != "") {
						Const.TEMHUM_IP = TEMHUM_IP;
						Const.TEMHUM_PORT = Integer.parseInt(TEMHUM_PORT);
						Const.TUBE_IP = TUBE_IP;
						Const.TUBE_PORT = Integer.parseInt(TUBE_PORT);
						Const.BUZZER_IP = BUZZER_IP;
						Const.BUZZER_PORT = Integer.parseInt(BUZZER_PORT);
						Const.FAN_IP = FAN_IP;
						Const.FAN_PORT = Integer.parseInt(FAN_PORT);
						Const.time = Integer.parseInt(time);
					} else {
						Toast.makeText(context, "配置信息不正确,请重输！", Toast.LENGTH_SHORT).show();
						return;
					}

					// 获取当前选择器的值
					Const.maxLim = maxLim_et.getValue();

					// 开启任务
					connectTask = new ConnectTask(context, tem_tv, hum_tv, info_tv);
					connectTask.setCIRCLE(true);
					connectTask.execute();
				} else {
					// 取消任务
					if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
						connectTask.setCIRCLE(false);
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
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
	 * 
	 * @param IP
	 * @param port
	 * @return
	 */
	private boolean checkIpPort(String IP, String port) {
		boolean isIpAddress = false;
		boolean isPort = false;

		if (IP == null || IP.length() < 7 || IP.length() > 15 || "".equals(IP) || port == null || port.length() < 4
				|| port.length() > 5) {
			return false;
		}
		// 判断IP格式和范围
		String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

		Pattern pat = Pattern.compile(rexp);

		Matcher mat = pat.matcher(IP);

		isIpAddress = mat.find();

		// 判断端口
		int portInt = Integer.parseInt(port);
		if (portInt > 1024 && portInt < 65536) {
			isPort = true;
		}

		return (isIpAddress && isPort);
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
