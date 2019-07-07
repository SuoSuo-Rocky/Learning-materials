package com.fro.util;

import android.util.Log;

/**
 * Created by Jorble on 2016/5/26.
 */
public class FRORfid {
	private static final String TAG = "FRORfid";

	/*******************************************************************************************
	 *                                        寻卡                                                                                                               *
	 *******************************************************************************************/
	// 寻卡正常：AA BB 08 00 00 00 01 02 00 04 00 07
	private static final String FIND_RIGHT = "AA BB 08 00 00 00 01 02 00 04 00 07";
	// 寻卡异常：AA BB 06 00 00 00 01 02 14 14
	private static final String FIND_ERROR = "AA BB 06 00 00 00 01 02 14 14";

	/**
	 * 是否寻卡成功
	 *
	 * @param read_buff
	 * @return
	 */
	public static boolean isFound(byte[] read_buff) {

		// 如果在传入参数正常才进行判断
		if (read_buff != null && read_buff.length >= 10) {
			// 读取返回字符串
			String readStr = HexStrConvertUtil.bytesToHexString(read_buff).toUpperCase();
			// 判断是否正常(如果返回字符串等于正确码则返回true,反之则反)
			return readStr.equals(FIND_RIGHT.replace(" ", "").toUpperCase()) ? true : false;
		}

		return false;
	}
	
	/*******************************************************************************************
	 *                                        读卡号                                                                                                               *
	 *******************************************************************************************/

	// 读卡号正常：AA BB 0A 00 00 00 02 02 00 5A EF C6 04 77
	private static final String READ_ID_RIGHT = "AA BB 0A 00 00 00 02 02 00 5A EF C6 04 77";
	// 读卡号异常：AA BB 06 00 00 00 02 02 0A 0A
	private static final String READ_ID_ERROR = "AA BB 06 00 00 00 02 02 0A 0A";

	/**
	 * 读出卡号： AA BB 0A 00 00 00 02 02 00 5A EF C6 04 77 
	 * 
	 * 其中5A EF C6 04
	 * 就是读取的卡号，不同的卡的卡号是不同的，这样卡号后面的检验码也不同。
	 *
	 * @param read_buff
	 * @return
	 */
	public static String getCardId(byte[] read_buff) {

		// 卡号ID
		String cardId = null;
		// 如果在传入参数正常才进行判断
		if (read_buff != null && read_buff.length >= 10) {
			// 读取返回字符串
			String readStr = HexStrConvertUtil.bytesToHexString(read_buff).toUpperCase();
			// 截取倒数第12-3位（"5AEFC604"）
			return readStr.substring(readStr.length() - 10, readStr.length() - 2);
		}

		return cardId;
	}

}
