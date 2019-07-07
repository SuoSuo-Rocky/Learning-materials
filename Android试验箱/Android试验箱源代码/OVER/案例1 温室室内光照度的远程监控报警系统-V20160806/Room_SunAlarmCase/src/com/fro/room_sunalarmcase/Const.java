package com.fro.room_sunalarmcase;
public class Const {

	public static String TAG="CASE";
	
	//光照度
	public static String SUN_CHK= "01 03 00 2a 00 01 a5 c2";
	public static int SUN_NUM=1;
	public static int SUN_LEN=7;
	public static Integer sun=null;
	public static Integer maxLim=700;
	
	//数码管
	public static String TUBE_CMD= "01 10 00 5e 00 02 04 12 11 03 17 62 9c";
	
	//蜂鸣器
	public static String BUZZER_ON= "01 10 00 5a 00 02 04 01 00 00 00 77 10";
	public static String BUZZER_OFF= "01 10 00 5a 00 02 04 00 00 00 00 76 ec";
	public static boolean isBuzzerOn=false;
	
	//窗帘
	public static String CURTAIN_ON= "01 10 00 4a 00 01 02 02 bb e9 29";
	public static boolean isCurtainOn=false;
	
	//IP端口
	public static String SUN_IP= "192.168.0.103";
	public static int SUN_PORT=4001;
	public static String TUBE_IP= "192.168.0.106";
	public static int TUBE_PORT=4001;
	public static String BUZZER_IP= "192.168.0.107";
	public static int BUZZER_PORT=4001;
	public static String CURTAIN_IP= "192.168.0.108";
	public static int CURTAIN_PORT=4001;
	
	//配置
	public static Integer time=500;
	public static Boolean linkage=true;
}
