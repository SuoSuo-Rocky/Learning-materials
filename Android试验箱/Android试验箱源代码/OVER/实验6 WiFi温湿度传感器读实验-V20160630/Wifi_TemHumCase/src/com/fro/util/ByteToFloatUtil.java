package com.fro.util;

/**
 * Created by Jorble on 2016/2/29.
 */
public class ByteToFloatUtil {
	
    /**
     * 解析单数值型传感器数据，16进制byte转换为float
	 * 例如：0x01 0xFF -> 511
     * @param b
     * @return
     */
    public static float hBytesToFloat(byte[] b) {			
        int s = 0;
        if (b[0] >= 0) {
            s = s + b[0];
        } else {
            s = s + 256 + b[0];
        }
        s = s * 256;
        if (b[1] >= 0) {
            s = s + b[1];
        } else {
            s = s + 256 + b[1];
        }
        float result = (float) s;
        return result;
    }

}
