package com.huflit.qlnhatro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Thời gian hiển thị splashscreen (ms)
    private static final long SPLASH_DELAY = 2200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Sau SPLASH_DELAY ms, mở màn đăng nhập rồi finish Splashsceen
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, DangnhapActivity.class));
            finish();
        }, SPLASH_DELAY);
    }
}
