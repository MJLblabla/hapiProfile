package com.hapi.aoptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("aa","aa")
        setContentView(R.layout.activity_main2)
    }
}
