package com.example.corporate;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.ImageView;
import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_SCREEN = 3000;
    ImageView logo;
    EditText appName;
    LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);
        appName = findViewById(R.id.appName);
        lottieAnimationView = findViewById(R.id.lottie);

        logo.animate().translationX(0).setDuration(500);
        appName.animate().translationX(0).setDuration(500);
        lottieAnimationView.animate().translationX(0).setDuration(500);

        logo.postDelayed(new Runnable() {
            @Override
            public void run() {
                logo.animate().rotation(360).setDuration(1100);
                appName.setCursorVisible(true);
            }
        }, 700);

        appName.postDelayed(new Runnable() {
            @Override
            public void run() {
                appName.setText("C");
                appName.setSelection(appName.getText().length());
            }
        }, 1000);
        appName.postDelayed(new Runnable() {
            @Override
            public void run() {
                appName.append("o");
            }
        }, 1100);
        appName.postDelayed(new Runnable() {
            @Override
            public void run() {
                appName.append("r");
            }
        }, 1200);
        appName.postDelayed(new Runnable() {
            @Override
            public void run() {
                appName.append("p");
            }
        }, 1300);
        appName.postDelayed(new Runnable() {
            @Override
            public void run() {
                appName.append("o");
            }
        }, 1400);
        appName.postDelayed(new Runnable() {
            @Override
            public void run() {
                appName.append("R");
            }
        }, 1500);
        appName.postDelayed(new Runnable() {
            @Override
            public void run() {
                appName.append("a");
            }
        }, 1600);
        appName.postDelayed(new Runnable() {
            @Override
            public void run() {
                appName.append("t");
            }
        }, 1700);
        appName.postDelayed(new Runnable() {
            @Override
            public void run() {
                appName.append("e");
            }
        }, 1800);

        logo.postDelayed(new Runnable() {
            @Override
            public void run() {
                appName.setCursorVisible(false);
                logo.animate().translationX(800).setDuration(500);
                appName.animate().translationX(800).setDuration(500);
                lottieAnimationView.animate().translationX(-800).setDuration(500);
            }
        }, 2500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_SCREEN);
    }
}