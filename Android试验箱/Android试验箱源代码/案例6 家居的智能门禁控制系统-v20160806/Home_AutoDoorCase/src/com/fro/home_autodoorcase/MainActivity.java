package com.fro.home_autodoorcase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dd.CircularProgressButton;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private Context context;

	private EditText rfidIp_et;
	private EditText rfidPort_et;
	private EditText fanIp_et;
	private EditText fanPort_et;

	private TextView readResult_tv;
	private TextView cardId_tv;
	private CircularProgressButton connectCircularButton;

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
		rfidIp_et = (EditText) findViewById(R.id.rfidIp_et);
		rfidPort_et = (EditText) findViewById(R.id.rfidPort_et);
		fanIp_et = (EditText) findViewById(R.id.fanIp_et);
		fanPort_et = (EditText) findViewById(R.id.fanPort_et);

		connectCircularButton = (CircularProgressButton) findViewById(R.id.connectCircularButton);
		cardId_tv = (TextView) findViewById(R.id.cardId_tv);
		readResult_tv = (TextView) findViewById(R.id.readResult_tv);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		rfidIp_et.setText(Const.RFID_IP);
		rfidPort_et.setText(String.valueOf(Const.RFID_PORT));
		fanIp_et.setText(Const.FAN_IP);
		fanPort_et.setText(String.valueOf(Const.FAN_PORT));
	}

	/**
	 * 按钮监听
	 */
	private void initEvent() {

		// 连接
		connectCircularButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (connectCircularButton.getProgress() == 0) {
					readResult_tv.setText("- - - -");
					cardId_tv.setText("- - - - - - - -");
					Const.CARD_ID=null;
					// 获取IP和端口
                    String RFID_IP = rfidIp_et.getText().toString().trim();
                    String RFID_PORT = rfidPort_et.getText().toString().trim();
                    String FAN_IP = fanIp_et.getText().toString().trim();
                    String FAN_PORT = fanPort_et.getText().toString().trim();
                    if(checkIpPort(RFID_IP, RFID_PORT) &&checkIpPort(FAN_IP, FAN_PORT)){
                    	Const.RFID_IP=RFID_IP;
                    	Const.RFID_PORT=Integer.parseInt(RFID_PORT);
                    	Const.FAN_IP=FAN_IP;
                    	Const.FAN_PORT=Integer.parseInt(FAN_PORT);
                    }else{
                        Toast.makeText(context, "配置信息不正确,请重输！", Toast.LENGTH_SHORT).show();
                        return;
                    }
					// 开启任务
					connectTask = new ConnectTask(context, readResult_tv, cardId_tv, connectCircularButton);
					connectTask.setCIRCLE(true);
					connectTask.execute();
					// 进度动画
					simulateSuccessProgress(connectCircularButton);
				} else {
					//恢复进度
					connectCircularButton.setProgress(0);
					// 取消任务
					if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
						connectTask.setCIRCLE(false);
						// 如果Task还在运行，则先取消它
						connectTask.cancel(true);
					}
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

	// 进度动画
	private void simulateSuccessProgress(final CircularProgressButton button) {
		ValueAnimator widthAnimation = ValueAnimator.ofInt(1, 80);
		widthAnimation.setDuration(1500);
		widthAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Integer value = (Integer) animation.getAnimatedValue();
				button.setProgress(value);
			}
		});
		widthAnimation.start();
	}

}
