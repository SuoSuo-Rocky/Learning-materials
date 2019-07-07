package com.fro.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.fro.home_autodoorcase.Const;

import android.util.Log;

/**
 * Created by Jorble on 2016/3/1.
 */
public class StreamUtil {
    private InputStream mis;
    private OutputStream mos;

    public StreamUtil(InputStream mis, OutputStream mos) {
        this.mis = mis;
        this.mos = mos;
    }

	/**
	 * 注意：要在子线程中调用
	 * 建立连接并返回socket，若连接失败返回null
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	private static Socket getSocket(String ip, int port) {
		Socket mSocket = new Socket();
		InetSocketAddress mSocketAddress = new InetSocketAddress(ip, port);
		// socket连接
		try {
			// 设置连接超时时间为3秒
			mSocket.connect(mSocketAddress, 3000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 检查是否连接成功
		if (mSocket.isConnected()) {
			Log.i(Const.TAG, ip + "连接成功！");
			return mSocket;
		} else {
			Log.i(Const.TAG, ip + "连接失败！");
			return null;
		}
	}
	
    /**
     * 写命令
     * 传入参数的形式="01 03 00 3c 00 01 44 06"
     * @param command
     */
    public static void writeCommand(OutputStream mos,String command) {
        byte[] writeBuff = null;
        if(mos!=null) {
            try {
                if (writeBuff == null) {
                    // 将指定字符串src，以每两个字符分割转换为16进制形式
                    writeBuff = HexStrConvertUtil.hexStringToByte(command);
                }
                mos.write(writeBuff);// 写命令
                mos.flush();// 发送并清空内存流
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取返回数据
     * @return read_buff
     */
    public static byte[] readData(InputStream mis){
        int len=0;
        byte[] read_buff=null;
        if(mis!=null) {
            try {
                len = mis.available();
                read_buff = new byte[len];
                mis.read(read_buff);//读取返回数据
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return read_buff;
    }
}
