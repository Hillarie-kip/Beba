package com.techkip.bebadriver.stupdstuff.Dev;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.techkip.bebadriver.R;


public class ProfileDev extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_view);
        DeveloperInfo.with(this).init().loadAbout();
    }
}