package com.example.iotproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView txtAppName;
    ConstraintLayout constraintLayout;

    Animation txtAnimation, layoutAnimation;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fall_down);
        layoutAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_to_top);

        txtAppName = findViewById(R.id.appName);
        constraintLayout = findViewById(R.id.conMain);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                constraintLayout.setVisibility(View.VISIBLE);
                constraintLayout.setAnimation(layoutAnimation);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        txtAppName.setVisibility(View.VISIBLE);
                        txtAppName.setAnimation(txtAnimation);
                    }
                }, 900);
            }
        }, 500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        }, 6000);
    }
}