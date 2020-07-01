package com.hapi.aoptest;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hapi.aopbeat.MethodBeatMonitorJava;


public class MainActivity extends AppCompatActivity {

    View login ;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MethodBeatMonitorJava.checkDeep();

        setContentView(R.layout.activity_login);
        login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new S().a();
                new S().b();
            }
        });
        new S().b();
    }
}
