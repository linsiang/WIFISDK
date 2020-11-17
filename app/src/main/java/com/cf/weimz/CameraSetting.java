package com.cf.weimz;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import com.cf.wifi.TcpSender;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class CameraSetting extends Activity {
    public Button btn_to_back;
    public Button btn_to_main;
    public Button btn_wifi_confirm;
    public Button btn_wifi_searchdevice;
    private RadioGroup myRadioGroupproduct;
    private RadioGroup myRadioGroupmode;
    private RadioGroup myRadioGroupwifi;
    public EditText ssid;
    public EditText pwd;
    public EditText channel;
    String devip = "192.168.1.1";
    int validwifi = -1;
    String m_ssid = null, m_passwd = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setLanguage(getApplicationContext());
        setContentView(R.layout.camera_settingtcf);

        btn_to_back = (Button) findViewById(R.id.btn_to_back);
        btn_to_main = (Button) findViewById(R.id.btn_to_main);

        btn_wifi_confirm = (Button) findViewById(R.id.btn_wifi_confirm);
        btn_wifi_searchdevice = (Button) findViewById(R.id.btn_wifi_searchdevice);

        myRadioGroupproduct = (RadioGroup) findViewById(R.id.myRadioGroupproduct);
        myRadioGroupwifi = (RadioGroup) findViewById(R.id.myRadioGroupwifi);
        myRadioGroupmode = (RadioGroup) findViewById(R.id.myRadioGroupmode);

        ssid = (EditText) findViewById(R.id.ssid);
        pwd = (EditText) findViewById(R.id.pwd);
        channel = (EditText) findViewById(R.id.channel);

        ReadCameraConfig();

        ssid.setText(g_wifissid);
        pwd.setText(g_wifipwd);
        channel.setText(Integer.toString(g_wifichannel));

        if (g_cameramode == 0)
            myRadioGroupmode.check(R.id.x50);
        else if (g_cameramode == 1)
            myRadioGroupmode.check(R.id.x200);

        if (g_wifimode == 0) {
            myRadioGroupwifi.check(R.id.AP);
            ssid.setText("mlg_Mxxx");
            pwd.setText("88888888");
        } else if (g_wifimode == 1) {
            myRadioGroupwifi.check(R.id.ROUTER);
            ReadSSID_PWD();
        } else if (g_wifimode == 2)
            myRadioGroupwifi.check(R.id.HOTSPOT);

        if (g_cameratype == 0)
            myRadioGroupproduct.check(R.id.bm558);
        if (g_cameratype == 1)
            myRadioGroupproduct.check(R.id.bm999);
        if (g_cameratype == 2)
            myRadioGroupproduct.check(R.id.bm189a);

        myRadioGroupmode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.x50 == checkedId) {
                    Log.e("iMVR", "x50");
                    g_cameramode = 0;
                    SaveCameraConfig();
                }
                if (R.id.x200 == checkedId) {
                    Log.e("iMVR", "x200");
                    g_cameramode = 1;
                    SaveCameraConfig();
                }

            }
        });

        myRadioGroupproduct.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.bm558 == checkedId) {
                    g_cameratype = 0;
                    Log.e("iMVR", "bm558");
                    SaveCameraConfig();
                }
                if (R.id.bm999 == checkedId) {
                    g_cameratype = 1;
                    Log.e("iMVR", "bm999");
                    SaveCameraConfig();
                }
                if (R.id.bm189a == checkedId) {
                    Log.e("iMVR", "bm189a");
                    g_cameratype = 2;
                    SaveCameraConfig();
                }
            }
        });

        myRadioGroupwifi.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.AP == checkedId) {
                    Log.e("iMVR", "AP");
                    g_wifimode = 0;
                    ssid.setText("mlg_Mxxx");
                    pwd.setText("88888888");
                    SaveCameraConfig();
                }
                if (R.id.ROUTER == checkedId) {
                    Log.e("iMVR", "ROUTER");
                    g_wifimode = 1;
                    SaveCameraConfig();
                    ReadSSID_PWD();
                }
                if (R.id.HOTSPOT == checkedId) {
                    Log.e("iMVR", "HOTSPOT");
                    g_wifimode = 2;
                    SaveCameraConfig();
                }
            }
        });

        btn_to_back.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                Log.e("iMVR", "btn_to_back");
                Intent intent = new Intent();
                intent.setClass(CameraSetting.this, FacedetectActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btn_to_main.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                Log.e("iMVR", "btn_to_main");
                Intent intent = new Intent();
                intent.setClass(CameraSetting.this, FacedetectActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btn_wifi_confirm.setOnClickListener(view -> {
            Log.e("iMVR", "btn_wifi_confirm");
            ShowConfirmDialog();
        });
        btn_wifi_searchdevice.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                Log.e("iMVR", "btn_wifi_searchdevice");
                String workmode;
                if (FindDevice() == 1) {
                    //	Log.e("iMVR","Find device ok");
                    devip.trim();
                    if (devip.contains("192.168.43")) {
                        g_wifimode = 2;
                        myRadioGroupwifi.check(R.id.HOTSPOT);
                        workmode = "Hotspot";
                    } else {
                        if (devip.equals("192.168.1.1")) {
                            g_wifimode = 0;
                            myRadioGroupwifi.check(R.id.AP);
                            workmode = "AP";
                        } else {
                            g_wifimode = 1;
                            myRadioGroupwifi.check(R.id.ROUTER);
                            workmode = "Router";
                        }
                    }

                    String Device = "Find Device-> ip:" + devip + "  " + "WifiMode: " + workmode;
                    Toast toast;
                    toast = Toast.makeText(getApplicationContext(), Device, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    if (getwifi() == 1) {
	                		/*	g_wifimode = TcpSender.getwifimode();
	                			if(g_wifimode==0)
		            				myRadioGroupwifi.check(R.id.AP);
		            			else if(g_wifimode==1)
		            				myRadioGroupwifi.check(R.id.ROUTER);
								else if(g_wifimode==2)
									myRadioGroupwifi.check(R.id.HOTSPOT);
							*/
                        SaveCameraConfig();
                    }
                } else {
                    Toast toast;
                    toast = Toast.makeText(getApplicationContext(), "No Device!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    private void ShowConfirmDialog() {
        AlertDialog.Builder normalDia = new AlertDialog.Builder(CameraSetting.this);
        normalDia.setIcon(R.drawable.icon);
        normalDia.setTitle("");
        normalDia.setMessage("Save change?");
        normalDia.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            //  @Override
            public void onClick(DialogInterface dialog, int which) {

                g_wifissid = ssid.getText().toString().trim();
                g_wifipwd = pwd.getText().toString().trim();
                String wifichannel = channel.getText().toString().trim();
                g_wifichannel = Integer.parseInt(wifichannel);

                if (g_wifimode == 0) {
                    if (g_wifipwd.getBytes().length != 8) {
                        Toast toast;
                        toast = Toast.makeText(getApplicationContext(), "Password length !=8", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return;
                    }
                }
                if (g_wifichannel < 1 || g_wifichannel > 10) {
                    Toast toast;
                    toast = Toast.makeText(getApplicationContext(), "Channel:<1-10>", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                Log.e("iMVR", "g_wifissid:" + g_wifissid + "g_wifipwd:" + g_wifipwd + "g_wifichannel:" + g_wifichannel + "g_wifimode:" + g_wifimode);

                Toast toast = null;
                int mode = 0;
                if (g_wifimode == 0) mode = 0; //ap
                else mode = 3;  //router or hotspot

                if (g_wifimode == 0) {
                    toast = Toast.makeText(getApplicationContext(), "SSID: mlgxxx Password: 88888888. Cannot be modified in AP mode!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                Log.e("iMVR", "=======setwifiinfoï¼š======:" + mode);
                int ret = TcpSender.setwifiinfo(mode, g_wifichannel, g_wifissid, g_wifipwd);
                Toast.makeText(CameraSetting.this, ret + "", Toast.LENGTH_SHORT).show();
                if (ret == 1) {
                    SaveSSID_PWD();
                    toast = Toast.makeText(getApplicationContext(), "save successful", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                } else {
                    toast = Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_LONG);

                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                }
            }
        });
        normalDia.setNegativeButton("No", new DialogInterface.OnClickListener() {
            // @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        normalDia.create().show();
    }

    public int FindDevice() {
        int findevice = 0;
        byte[] ip = new byte[16];
        if (g_wifimode == 2)
            TcpSender.SetDeviceType(2); //hotspot mode
        else
            TcpSender.SetDeviceType(0); //ap  or router mode!

        findevice = TcpSender.SearchDevice(ip);
        if (findevice == 1) {
            try {
                devip = new String(ip, "GB2312");  //以gb2312的格式进行解码，返回的是字符串
                devip = devip.trim();   //去除头尾空格
                Log.e("iMVR", "--------devip----------------:" + devip);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return findevice;
    }

    public int getwifi() {
        byte[] t_ssid = new byte[32];
        byte[] t_pwd = new byte[20];
        int ap = 1, routerorhotspot = 4;
        int mode = 0;
        if (g_wifimode == 0) {
            mode = ap;
        } else {
            mode = routerorhotspot;
        }
        validwifi = TcpSender.getwifiinfo(mode);
        if (validwifi == 100) {
            TcpSender.getwifissid(t_ssid);
            TcpSender.getwifipwd(t_pwd);
            try {
                m_ssid = new String(t_ssid, "GB2312");
                m_passwd = new String(t_pwd, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (g_wifimode == 0) {
                pwd.setText(m_passwd.trim());
                ssid.setText(m_ssid.trim());
            } else {
                pwd.setText(m_passwd.trim());
                ssid.setText(m_ssid.trim());
            }
        } else {
            pwd.setText(null);
            ssid.setText(null);

            Toast toast;
            toast = Toast.makeText(getApplicationContext(), "Unable to obtain WiFi information, please detect network connections!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
        return validwifi;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {

            Log.e("iMVR", "stop video");

            Intent intent = new Intent();
            intent.setClass(CameraSetting.this, FacedetectActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private int g_english = 0;

    private boolean ReadLanguageConfig() {
        SharedPreferences sharedata = getSharedPreferences("tcflanguageconfig", 0);
        g_english = sharedata.getInt("language", 0);
        return true;
    }

    private void setLanguage(Context mContext) {
        Resources resources = getResources();
        ReadLanguageConfig();
        Log.e("iMVR", "g_english:" + g_english);
        final Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        switch (g_english) {
            case 0:
                configuration.locale = Locale.ENGLISH;
                break;
            case 1:
                configuration.locale = Locale.SIMPLIFIED_CHINESE;
                break;
            case 2:
                configuration.locale = Locale.TRADITIONAL_CHINESE;
                break;
            case 3:
                configuration.locale = Locale.JAPANESE;
                break;
            case 4:
                configuration.locale = Locale.FRENCH;
                break;
            default:
                configuration.locale = Locale.getDefault();
                break;
        }
        Log.e("iMVR", "configuration==" + configuration.locale);
        resources.updateConfiguration(configuration, displayMetrics);
    }

    private int g_cameratype = 0;
    private int g_cameramode = 0;
    private String g_wifiip = null;
    private String g_wifissid = null;
    private String g_wifipwd = null;

    private int g_wifichannel = 6;
    private int g_wifimode = 0;
    private int g_wififinddevice = 0;

    private boolean ReadCameraConfig() {
        SharedPreferences sharedata = getSharedPreferences("tcfcameraN", 0);
        g_cameratype = sharedata.getInt("cameratype", 0);
        g_cameramode = sharedata.getInt("cameramode", 0);
        g_wifiip = sharedata.getString("wifiip", "192.168.1.1");
        g_wifissid = sharedata.getString("wifissid", "mlgxxx");
        g_wifipwd = sharedata.getString("wifipwd", "88888888");
        g_wifichannel = sharedata.getInt("wifichannel", 6);
        g_wifimode = sharedata.getInt("wifimode", 0);
        g_wififinddevice = sharedata.getInt("wififinddevice", 0);
        return true;
    }

    boolean SaveCameraConfig() {
        SharedPreferences.Editor sharedata = getSharedPreferences("tcfcameraN", 0).edit();
        sharedata.putInt("cameratype", g_cameratype);
        sharedata.putInt("cameramode", g_cameramode);

        sharedata.putString("wifiip", g_wifiip);
        sharedata.putString("wifissid", g_wifissid);
        sharedata.putString("wifipwd", g_wifipwd);
        sharedata.putInt("wifichannel", g_wifichannel);
        sharedata.putInt("wifimode", g_wifimode);
        sharedata.putInt("wififinddevice", g_wififinddevice);
        sharedata.apply();
        return true;
    }

    private void SaveSSID_PWD() {
        SharedPreferences.Editor shaper = getSharedPreferences("SSIS_PWD", MODE_PRIVATE).edit();
        shaper.putString("ssid", ssid.getText().toString());
        shaper.putString("pwd", pwd.getText().toString());
        shaper.apply();
    }

    private void ReadSSID_PWD() {
        SharedPreferences sharedPreferences = getSharedPreferences("SSIS_PWD", MODE_PRIVATE);
        ssid.setText(sharedPreferences.getString("ssid", "TP-LINK_76E1"));
        pwd.setText(sharedPreferences.getString("pwd", "aa123456"));
    }
}
