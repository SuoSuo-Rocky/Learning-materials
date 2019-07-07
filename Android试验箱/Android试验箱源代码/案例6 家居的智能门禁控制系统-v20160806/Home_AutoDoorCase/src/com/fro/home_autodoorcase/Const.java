package com.fro.home_autodoorcase;
public class Const {

	public static String TAG="CASE";
	
	//RFID
	public static String RFID_FIND= "AA BB 06 00 00 00 01 02 52 51";
	public static String RFID_READ= "AA BB 06 00 00 00 02 02 04 04";
	public static String CARD_ID=null;
	
	//电机
	public static String FAN_ON= "01 10 00 48 00 01 02 00 01 68 18";
	public static String FAN_OFF= "01 10 00 48 00 01 02 00 02 28 19";
	public static boolean isFanOn=false;
	
	//IP端口
	public static String RFID_IP= "192.168.0.109";
	public static int RFID_PORT=4001;
	public static String FAN_IP= "192.168.0.105";
	public static int FAN_PORT=4001;
	
	//配置
	public static Integer time=500;
	public static Boolean linkage=true;
}
