package com.fro.sms_sendcase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Button btn;
	private EditText phoneEt, contextEt;
	private TextView sendMsgTv;
	private StringBuilder sendMsgSb = new StringBuilder();
	
	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 电话号码
			String phone = phoneEt.getText().toString();
			// 短信内容
			String context = contextEt.getText().toString();
			// 获得默认的消息管理器
			SmsManager manager = SmsManager.getDefault();
			// 因为一条短信有字数限制，因此要将长短信拆分
			ArrayList<String> list = manager.divideMessage(context); 
			for (String text : list) {
				// 发送信息
				manager.sendTextMessage(phone, null, text, null, null);
			}
			sendMsgTv.setText(sendMsgSb.append(context + '\n'));
			Toast.makeText(getApplicationContext(), "发送完毕", Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btn = (Button) this.findViewById(R.id.send_bt);
		phoneEt = (EditText) this.findViewById(R.id.phone_et);
		contextEt = (EditText) this.findViewById(R.id.msg_et);
		sendMsgTv = (TextView) this.findViewById(R.id.SendMsg_tv);
		btn.setOnClickListener(listener);
	}

}
