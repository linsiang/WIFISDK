package com.cf.weimz;
import java.io.File;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
public class FacedetectActivity extends BaseActivity {
    private Button main_settings;
    private Button main_language;
    private Button main_video;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.login);
        main_settings = findViewById(R.id.main_settings);
        main_language = findViewById(R.id.main_language);
        main_video = findViewById(R.id.main_video);
        main_settings.setOnClickListener(v -> {
            Intent intent1 = new Intent();
            intent1.setClass(FacedetectActivity.this, CameraSetting.class);
            startActivity(intent1);
            finish();
        });
        main_language.setOnClickListener(v -> {
            if (ReadLanguageConfig() == 1) {
                SaveLanguageConfig(0);
            } else if (ReadLanguageConfig() == 0) {
                SaveLanguageConfig(1);
            }
            setLanguage();
            startActivity(new Intent(getApplicationContext(), FacedetectActivity.class));
            finish();
        });
        main_video.setOnClickListener(v -> {
            String picpath = getSDPath() + "/" + "10000";
            File destDir2 = new File(picpath);
            if (!destDir2.exists()) {
                if (destDir2.mkdirs()) {
                    Log.e("IMVR", "onItemClick: 创建文件夹成功！");
                } else {
                    Log.e("IMVR", "onItemClick: 创建文件夹失败！");
                }
            } else {
                Log.e("IMVR", "onItemClick: 10000文件夹已经存在！");
            }
            Intent intent = new Intent();
            intent.setClass(FacedetectActivity.this, VideoSDK.class);
            startActivity(intent);
            finish();
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        return sdDir != null ? sdDir.toString() : null;
    }
}





