package com.example.claire.assignment2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This is a welcome screen used as launcher.
 */

public class WelcomeActivity extends AppCompatActivity {

    private TextView welTV;
    private ImageView welIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        welTV = (TextView) findViewById(R.id.welcomeTV);
        welIV = (ImageView) findViewById(R.id.welcomeIV);

        Timer timer = new Timer();
        //create a timerTask object
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        };
        //scheduled as running after 3 seconds
        timer.schedule(timerTask, 3000);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
// TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation== Configuration.ORIENTATION_LANDSCAPE) {
        }
        else{
        }
    }

}
