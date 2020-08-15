package com.techkip.bebarider.stupidstuff.Dev;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.techkip.bebarider.R;



public class ProfileDev extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_view);
        DeveloperInfo.with(this).init().loadAbout();
    }
}