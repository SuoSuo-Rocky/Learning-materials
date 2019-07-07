package com.fro.util;

/**
 * Created by Jorble on 2016/3/24.
 */
public class FROIOControl {

    /**
     * 解析单数据型数据
     * @param rightLen
     * @param nodeNum
     * @param read_buff
     * @return
     */
    public static Boolean isSuccess(int rightLen, int nodeNum,byte[] read_buff) {

        if (read_buff!=null) {
            // 长度是否正确，节点号是否正确
            if ((read_buff.length == rightLen && read_buff[0] == nodeNum)) {
                return true;
            }else {
                return false;
            }
        }
        return false;// 返回数据
    }
}
