package com.fro.room_temhumalarmcase;
public class Const {

	public static String TAG="CASE";
	
	//温湿度
	public static String TEMHUM_CHK= "01 03 00 14 00 02 84 0f";
	public static int TEMHUM_NUM=1;
	public static int TEMHUM_LEN=9;
	public static Integer tem=null;
	public static Integer hum=null;
	public static Integer maxLim=30;
	
	//数码管
	public static String TUBE_CMD= "01 10 00 5e 00 02 04 12 11 03 17 62 9c";
	
	//蜂鸣器
	public static String BUZZER_ON= "01 10 00 5a 00 02 04 01 00 00 00 77 10";
	public static String BUZZER_OFF= "01 10 00 5a 00 02 04 00 00 00 00 76 ec";
	public static boolean isBuzzerOn=false;
	
	//风扇
	public static String FAN_ON= "01 10 00 48 00 01 02 00 01 68 18";
	public static String FAN_OFF= "01 10 00 48 00 01 02 00 02 28 19";
	public static boolean isFanOn=false;
	
	//IP端口
	public static String TEMHUM_IP= "192.168.0.100";
	public static int TEMHUM_PORT=4001;
	public static String TUBE_IP= "192.168.0.106";
	public static int TUBE_PORT=4001;
	public static String BUZZER_IP= "192.168.0.107";
	public static int BUZZER_PORT=4001;
	public static String FAN_IP= "192.168.0.105";
	public static int FAN_PORT=4001;
	
	//配置
	public static Integer time=500;
	public static Boolean linkage=true;
}
