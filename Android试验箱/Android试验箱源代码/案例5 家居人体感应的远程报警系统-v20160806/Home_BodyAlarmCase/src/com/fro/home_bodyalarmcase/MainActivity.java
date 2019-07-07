package com.fro.home_bodyalarmcase;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cunoraz.gifview.library.GifView;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
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
	private EditText buzzerIp_et;
	private EditText buzzerPort_et;

	private GifView body_gif;
	private ToggleButton connect_tb;
	private TextView info_tv;
	EditText phoneEt, contextEt;

	private final Timer timer = new Timer();
	private TimerTask task;

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

		// 定时检查是否有人才发送短信
		task = new TimerTask() {
			@Override
			public void run() {
				// 发送短信
				if (Const.BODY != null && Const.BODY && Const.SMS) {
					SmsManager manager = SmsManager.getDefault();
					String phone = phoneEt.getText().toString();
					String context = contextEt.getText().toString();
					if (isCanUseSim() && manager != null && phone != null && context != null) {
						Log.i(Const.TAG, "发送短信:" + phone + "-" + context);
						manager.sendTextMessage(phone, null, context, null, null);
						Const.SMS = false;// 只发送一次，发送完置false
					} else {
						Log.i(Const.TAG, "发送短信失败！");
					}
				}
			}
		};
		timer.schedule(task, 2000, 2000); // 定时
	}

	/**
	 * 绑定控件
	 */
	private void bindView() {
		bodyIp_et = (EditText) findViewById(R.id.bodyIp_et);
		bodyPort_et = (EditText) findViewById(R.id.bodyPort_et);
		buzzerIp_et = (EditText) findViewById(R.id.buzzerIp_et);
		buzzerPort_et = (EditText) findViewById(R.id.buzzerPort_et);

		connect_tb = (ToggleButton) findViewById(R.id.connect_tb);
		info_tv = (TextView) findViewById(R.id.info_tv);

		body_gif = (GifView) findViewById(R.id.body_gif);
		phoneEt = (EditText) findViewById(R.id.phoneEt);
		contextEt = (EditText) findViewById(R.id.contextEt);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		bodyIp_et.setText(Const.BODY_IP);
		bodyPort_et.setText(String.valueOf(Const.BODY_PORT));
		buzzerIp_et.setText(Const.BUZZER_IP);
		buzzerPort_et.setText(String.valueOf(Const.BUZZER_PORT));
		info_tv.setText("请点击连接!");

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
                    String BODY_IP = bodyIp_et.getText().toString().trim();
                    String BODY_PORT = bodyPort_et.getText().toString().trim();
                    String BUZZER_IP = buzzerIp_et.getText().toString().trim();
                    String BUZZER_PORT = buzzerPort_et.getText().toString().trim();
                    if(checkIpPort(BODY_IP, BODY_PORT) &&checkIpPort(BUZZER_IP, BUZZER_PORT)){
                    	Const.BODY_IP=BODY_IP;
                    	Const.BODY_PORT=Integer.parseInt(BODY_PORT);
                    	Const.BUZZER_IP=BUZZER_IP;
                    	Const.BUZZER_PORT=Integer.parseInt(BUZZER_PORT);
                    }else{
                        Toast.makeText(context, "配置信息不正确,请重输！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
					// 显示动画
					body_gif.setVisibility(View.VISIBLE);

					// 开启任务
					connectTask = new ConnectTask(context, info_tv, body_gif);
					connectTask.setCIRCLE(true);
					connectTask.execute();
				} else {
					// 取消动画
					body_gif.setVisibility(View.INVISIBLE);
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
    

	// sim卡是否可读
	public boolean isCanUseSim() {
		try {
			TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

			return TelephonyManager.SIM_STATE_READY == mgr.getSimState();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
