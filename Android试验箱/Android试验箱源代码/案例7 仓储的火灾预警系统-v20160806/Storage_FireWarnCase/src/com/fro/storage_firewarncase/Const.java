package com.fro.storage_firewarncase;

public class Const {

	public static String TAG = "CASE";

	// 温湿度
	public static String TEMHUM_CHK = "01 03 00 14 00 02 84 0f";
	public static int TEMHUM_NUM = 1;
	public static int TEMHUM_LEN = 9;
	public static Integer tem = null;
	public static Integer hum = null;
	public static Integer temMaxLim = 50;
	public static Integer humMinLim = 20;

	// 烟雾
	public static String SMOKE_CHK = "01 03 00 34 00 01 c5 c4";
	public static int SMOKE_NUM = 1;
	public static int SMOKE_LEN = 7;
	public static Integer smoke = null;
	public static Integer smokeMaxLim = 100;

	// 风机
	public static String FAN_ON = "01 10 00 48 00 01 02 00 01 68 18";
	public static String FAN_OFF = "01 10 00 48 00 01 02 00 02 28 19";
	public static boolean isFanOn = false;

	// IP端口
	public static String TEMHUM_IP = "192.168.0.100";
	public static int TEMHUM_PORT = 4001;
	public static String SMOKE_IP = "192.168.0.102";
	public static int SMOKE_PORT = 4001;
	public static String FAN_IP = "192.168.0.105";
	public static int FAN_PORT = 4001;

	// 配置
	public static Integer time = 500;
	public static Boolean linkage = true;
	public static Integer warnCount = 0;
}
