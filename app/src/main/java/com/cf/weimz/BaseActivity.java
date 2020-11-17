package com.cf.weimz;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.transition.Explode;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;


import com.cf.weimz.R;

import java.util.Locale;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全面屏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setEnterTransition(new Explode().setDuration(1000));
        getWindow().setExitTransition(new Explode().setDuration(1000));
        setLanguage();
        setContentView(R.layout.activity_base);
    }

    int ReadLanguageConfig() {
        SharedPreferences sharedata = getSharedPreferences("tcflanguageconfig", 0);
        return sharedata.getInt("language", 0);
    }

    void SaveLanguageConfig(int mlg_Language) {
        SharedPreferences.Editor sharepre = getSharedPreferences("tcflanguageconfig", 0).edit();
        sharepre.putInt("language", mlg_Language);
        sharepre.apply();
    }

    void setLanguage() {
        Resources resources = getResources();
        final Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        switch (ReadLanguageConfig()) {
            case 0:
                configuration.setLocale(Locale.ENGLISH);
                break;
            case 1:
                configuration.setLocale(Locale.SIMPLIFIED_CHINESE);
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
        resources.updateConfiguration(configuration, displayMetrics);
    }
}