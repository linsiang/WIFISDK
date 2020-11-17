package com.cf.weimz;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


////////////////////Video.jar -->libs////////////////
import com.cf.cf685.Video;

/////////////////////////////////////////////////////
public class VideoSDK extends Activity {

    public String picpath;
    int screen_height = 0, screen_width = 0;
    public Video VideoDecoder;
    public ImageView mypictureView;
    private Button videobuttonCapture;

    private Button videobuttonStart;
    private Button videobuttonStop;
    boolean running = false;
    public SurfaceView mSurfaceView;
    boolean isScale = true;
    private Button toMain;
    public String CurrentJPGFile;
    private ViewGroup.LayoutParams mrLayout;
    private boolean isAll = false;
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_KEY_LONG = "recentapps";

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {

                    Log.e("iMVR", "onReceive!");
                    unregisterReceiver(mHomeKeyEventReceiver);
                    VideoDecoder.StopKeyThread();
                    VideoDecoder.StopVideo();
                    //   StopCodec();

                    Intent intentM = new Intent();
                    intentM.setClass(VideoSDK.this, FacedetectActivity.class);
                    startActivity(intentM);
                    finish();

                } else if (TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)) {

                    Log.e("iMVR", "Home!");
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 10) {
                Toast toast = null;
                toast = Toast.makeText(getApplicationContext(), "There is no JPG picture in the TF Card!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Log.e("iMVR", "============msg.what==10=================");
            }
            if (msg.what == 11) {
                Toast toast = null;
                toast = Toast.makeText(getApplicationContext(), "The current picture was deleted!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                Log.e("iMVR", "Delete CurrentJPGFile:" + CurrentJPGFile);
                Log.e("iMVR", "============msg.what==11=================");
            }
            if (msg.what == 12) {
                Toast toast = null;
                toast = Toast.makeText(getApplicationContext(), "All the files are deleted in the TF Card!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Log.e("iMVR", "============msg.what==12=================");
                //DeleteAllJPG();
                //image_screenshot.setVisibility(View.INVISIBLE);
                //mSurfaceView.setVisibility(View.VISIBLE);
                //delAllFile(g_jpgpath);
            }
            if (msg.what == 13) {
                Log.e("iMVR", "msg.what==13:-->" + isScale);
                //	image_screenshot.setVisibility(View.INVISIBLE);
                //	mSurfaceView.setVisibility(View.VISIBLE);
            }
            if (msg.what == 6) {
                String filename = msg.getData().getString("filename");
                boolean newfile = msg.getData().getBoolean("newfile");
                Log.e("iMVR", "filename->6:" + filename);
                CurrentJPGFile = filename;
                Bitmap b = BitmapFactory.decodeFile(filename); //show picture！
                mypictureView.setImageBitmap(b);
                Log.e("iMVR", "============msg.what==6=================");

            }
            if (msg.what == 5) {
                int oil = msg.getData().getInt("oil");
                int water = msg.getData().getInt("water");
                // Toast.makeText(getApplicationContext(), filename, Toast.LENGTH_SHORT).show();
                Log.e("iMVR", "oil:" + oil + "water:" + water);
                //   Toast.makeText(VideoSDK.this, "oil"+oil+"water"+water, Toast.LENGTH_SHORT).show();
            }

            if (msg.what == 4) {
                String filename = msg.getData().getString("filename");//picture name！
                Log.e("iMVR", "filename:" + filename);

                String jpgfilepath = picpath + filename;
                Log.e("iMVR", "jpgfile:" + jpgfilepath);
                permissionActivity.verifyStoragePermissions(VideoSDK.this);
                Bitmap b = BitmapFactory.decodeFile(jpgfilepath); //test for capture picture！
                mypictureView.setImageBitmap(b);

                Log.e("iMVR", "============msg.what==4=================");
            }
            if (msg.what == 1) {
                Log.e("iMVR", "diagnosos--->lock ->msg.what==1");
                //  Toast.makeText(getApplicationContext(), "lock", Toast.LENGTH_SHORT).show();

            }
            if (msg.what == 2) {
                Log.e("iMVR", "diagnosos--->unlock");
                // Toast.makeText(getApplicationContext(), "unlock", Toast.LENGTH_SHORT).show();
                Log.e("iMVR", "============msg.what==2=================");
            }
            if (msg.what == 3) {
                Log.e("iMVR", "net is error!");  //disconnect device！
                Log.e("iMVR", "============msg.what==3=================");
            }

            /**
             * when use the BM999 you may use those message,
             */
    /*      if (msg.what == 20) {  // left button
                Log.e("iMVR", "============msg.what==20=================");
            }
            if (msg.what == 21) { //  right button
                Log.e("iMVR", "============msg.what==21=================");
            }
            if (msg.what == 22) { //delete button
                Log.e("iMVR", "============msg.what==22=================");
            }*/
        }
    };

    @SuppressLint({"NewApi", "ClickableViewAccessibility"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        screen_width = metric.widthPixels;
        screen_height = metric.heightPixels;
        setContentView(R.layout.cameracv1920);
        registerReceiver(mHomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        mSurfaceView = findViewById(R.id.surfaceView1);
        mSurfaceView.setFocusable(true);
        mSurfaceView.setFocusableInTouchMode(true);
        mSurfaceView.setVisibility(View.VISIBLE);
        videobuttonCapture = findViewById(R.id.face_capturebtn);
        toMain = findViewById(R.id.toMain);
        mypictureView = findViewById(R.id.mypicture);
        mypictureView.setVisibility(View.VISIBLE);
        videobuttonStart = findViewById(R.id.face_startbtn);
        videobuttonStop = findViewById(R.id.face_stopbtn);
        toMain.setOnClickListener(v -> {
            unregisterReceiver(mHomeKeyEventReceiver);
            startActivity(new Intent(this, FacedetectActivity.class));
            finish();
        });


        mSurfaceView.setOnTouchListener((View.OnTouchListener) (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isAll) {
                    Log.e("videosdk", "onCreate: 点击退出全屏。。。" );
                    mSurfaceView.setLayoutParams(mrLayout);
                    isAll=false;
                    return false;
                } else {
                    Log.e("videosdk", "onCreate: 点击设置全屏、、、" );
                    mrLayout = mSurfaceView.getLayoutParams();
                    mSurfaceView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                    isAll = true;
                    return true;
                }
            }
             return true;
        });


        videobuttonCapture.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) //
            {
                if (running)
                    VideoDecoder.ManualCapture(); //Hand capture
            }
        });

        videobuttonStart.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) //
            {
                VideoDecoder.FindDevice();
                Log.e("iMVR", "start video");
                if (!running) {
                    running = true;
                    VideoDecoder.StartKeyThread();  //
                    VideoDecoder.StartVideoThread();//
                }
            }
        });
        videobuttonStop.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) //
            {
                Log.e("iMVR", "stop video");
                if (running) {
                    VideoDecoder.StopKeyThread(); //
                    VideoDecoder.StopVideo(); //
                    running = false;
                }
            }
        });

        picpath = getSDPath() + "/10000/"; //  Snapshot folder
        VideoDecoder = new Video(mSurfaceView, handler, picpath, "192.168.1.1");  //IP , do not modify
        Log.e("surface init ---->>>>>>", "onCreate: 这里创建surface！");
        mSurfaceView.setVisibility(View.GONE);
        Log.e("surface stop? -->>>>>>", "onCreate: stopsurfaceviewc---");
        mSurfaceView.setVisibility(View.VISIBLE);
        //////////if you want to use router or hotspot mode!!!///////////////////////////////////
        //////////////////////////////////////////////////////////////////////
        SharedPreferences sharedata = getSharedPreferences("tcfcameraN", 0);
        int g_wifimode = sharedata.getInt("wifimode", 0);
        if (g_wifimode != 0) {
            VideoDecoder.SetDevType(g_wifimode);
        }
    /*
   //   VideoDecoder.SetBM999(1);   //if your device is BM999
        if (g_wifimode != 0) {
            VideoDecoder.SetDevType(g_wifimode);
            if (VideoDecoder.FindDevice() == 1) {
                Log.e("iMVR", "Find device ok!");
            } else {
                Toast toast;
                toast = Toast.makeText(getApplicationContext(), "Router Mode: No Device!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }*/
        ///////////////////////////////////////////////////////////////////////
        StartCodec();
    }

    public void StopCodec() {
        VideoDecoder.ReleaseCodec();//release codec....
    }

    public void StartCodec() {
        CodecClient m_codecclient = new CodecClient();
        Thread codecThread = new Thread(m_codecclient);
        codecThread.start();
    }

    class CodecClient implements Runnable {
        public void run() {
            new Thread(new ThreadCodec()).start();
        }
    }

    class ThreadCodec implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(700);  //Most mobile phones 500 can be realized, and some tablet phones can be set to 700
                VideoDecoder.InitCodec();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {

            unregisterReceiver(mHomeKeyEventReceiver);

            VideoDecoder.StopKeyThread(); //stop key  thread
            VideoDecoder.StopVideo(); //stop  video thread
            //   StopCodec();

            Intent intent = new Intent();
            intent.setClass(VideoSDK.this, FacedetectActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//
            //sdDir = getExternalFilesDir(null);
        }
        return sdDir.toString();
    }
}