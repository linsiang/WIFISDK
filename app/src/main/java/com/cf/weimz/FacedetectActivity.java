package com.cf.weimz;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class FacedetectActivity extends Activity implements OnClickListener {
    private GridView toolbarGrid;
    private final int TOOLBAR_ITEM_ADD = 0;//
    private final int TOOLBAR_ITEM_ENTER = 1;//
    int[] menu_toolbar_image_array = {R.drawable.tab11_nor, R.drawable.tab13_nor};
    String[] menu_toolbar_name_array;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.login);
        menu_toolbar_name_array = new String[2];
        menu_toolbar_name_array[0] = "登录";
        menu_toolbar_name_array[1] = "注册";

        toolbarGrid = findViewById(R.id.GridView_toolbar);
        toolbarGrid.setSelector(R.drawable.toolbar_menu_item);
        toolbarGrid.setBackgroundResource(R.drawable.menu_bg2);
        toolbarGrid.setNumColumns(2);
        toolbarGrid.setGravity(Gravity.BOTTOM);
        toolbarGrid.setVerticalSpacing(10);
        toolbarGrid.setHorizontalSpacing(10);
        toolbarGrid.setAdapter(getMenuAdapter(menu_toolbar_name_array,
                menu_toolbar_image_array));
        toolbarGrid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                switch (arg2) {
                    case TOOLBAR_ITEM_ADD:
                        Intent intent1 = new Intent();
                        intent1.setClass(FacedetectActivity.this, CameraSetting.class);
                        startActivity(intent1);
                        finish();
                        break;
                    case TOOLBAR_ITEM_ENTER:
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
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private SimpleAdapter getMenuAdapter(String[] menuNameArray,
                                         int[] imageResourceArray) {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < menuNameArray.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImage", imageResourceArray[i]);
            map.put("itemText", menuNameArray[i]);
            data.add(map);
        }
        SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
                R.layout.item_menu, new String[]{"itemImage", "itemText"},
                new int[]{R.id.item_image, R.id.item_text});
        return simperAdapter;
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

    public void onClick(View v) {
    }


}