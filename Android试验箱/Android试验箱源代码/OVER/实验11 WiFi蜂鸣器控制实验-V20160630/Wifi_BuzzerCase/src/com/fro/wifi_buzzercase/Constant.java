package com.fro.wifi_buzzercase;

public class Constant {

	// IP地址
	public static String IP = "192.168.0.107";
	// 端口
	public static int port = 4001;
	// 返回长度
	public static final int NODE_LEN = 13;
	// 节点号
	public static final int NODE_NUM = 1;
	// 命令
	public static final String CLOSEALL_CMD = "01 10 00 5a 00 02 04 00 00 00 00 76 ec";
	public static final String BUZZER_CMD = "01 10 00 5a 00 02 04 01 00 00 00 77 10";
	public static final String RED_CMD = "01 10 00 5a 00 02 04 00 01 00 00 27 2c";
	public static final String GREEN_CMD = "01 10 00 5a 00 02 04 00 00 01 00 77 7c";
	public static final String BLUE_CMD = "01 10 00 5a 00 02 04 00 00 00 01 b7 2c";
}
