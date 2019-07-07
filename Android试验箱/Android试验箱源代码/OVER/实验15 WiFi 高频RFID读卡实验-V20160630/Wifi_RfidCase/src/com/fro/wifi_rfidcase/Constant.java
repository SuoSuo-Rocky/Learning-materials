package com.fro.wifi_rfidcase;

public class Constant {

	//IP地址
	public static String IP="192.168.0.109";
	//端口
	public static int port=4001;
	
	public static String CARD_ID="5A EF C6 04";
	public static String AREA_DATA="00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";//17位
	
	//寻卡
	public static final String FIND_CARD_CMD="aa bb 06 00 00 00 01 02 52 51";
	public static final String FIND_CARD_SUCCESS="aa bb 08 00 00 00 01 02 00 04 00 07";
	public static final String FIND_CARD_ERROR="aa bb 06 00 00 00 01 02 14 14";
	
	//读卡
	public static final String READ_CARD_CMD="AA BB 06 00 00 00 02 02 04 04";
	public static String READ_CARD_SUCCESS="AA BB 0A 00 00 00 02 02 00 5A EF C6 04 77";
	public static final String READ_CARD_ERROR="aa bb 06 00 00 00 02 02 0a 0a";
	
	
	//选卡
	public static String CHOOSE_CARD_CMD="AA BB 09 00 00 00 03 02 5A EF C6 04 76";
	public static String CHOOSE_CARD_SUCCESS="AA BB 07 00 00 00 03 02 00 08 09";
	public static final String CHOOSE_CARD_ERROR="AA BB 06 00 00 00 03 02 0A 0A";
	
	//选块
	public static String CHOOSE_AREA_CMD="AA BB 0D 00 00 00 07 02 60 01 FF FF FF FF FF FF 64";
	public static String CHOOSE_AREA_SUCCESS="AA BB 06 00 00 00 07 02 00 05";
	public static final String CHOOSE_AREA_ERROR="AA BB 06 00 00 00 07 02 16 16";
	
	//读块
	public static final String READ_AREA_CMD="AA BB 06 00 00 00 08 02 01 0B";
	public static String READ_AREA_SUCCESS="AA BB 16 00 00 00 08 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A";
	public static final String READ_AREA_ERROR="AA BB 06 00 00 00 08 02 17 17";
	
}
