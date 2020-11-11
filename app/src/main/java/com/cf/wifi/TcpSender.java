package com.cf.wifi;
import android.util.Log;

public class TcpSender {
	static {
		System.loadLibrary("tcpsender");
		Log.e("iMVR","libtcpsender.so");
	}

	public static void Load(){}
	//============================================================
	//2018/12/29
	public static native int getwifissid(byte[] out);
	public static native int getwifipwd(byte[] out);
	public static native int getwifichannel();

	//mode: 0-AP  1:others
	public static native int getwifimode();
	
	//return 1: success  mode :0 AP  1 router
	public static native int setwifiinfo(int wifimode,int channel,String ssid_str,String pwd_str);
	
	//return 1 success  mode :0 AP  1 router
	public static native int getwifiinfo(int mode); 
	
	public static native int getdevip(byte[] out);
	
	//return 1: find device  out:ip
	public static native int
	SearchDevice(byte[] out);
	//0  1: (AP模式 /Route模式)  2:手机热点模式
	public static native int  SetDeviceType(int devicetype);
}
