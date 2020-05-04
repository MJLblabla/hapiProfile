package com.hapi.aoptest;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hapi.aop.Beat;
import com.hapi.aop.LoopTimer;
import com.hapi.aop.MethodBeatMonitor;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);

        new S().b();
    }
}
