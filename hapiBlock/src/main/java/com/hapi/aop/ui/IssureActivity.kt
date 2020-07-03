package com.hapi.aop.ui

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hapi.aop.R
import kotlinx.android.synthetic.main.activity_issure.*


class IssureActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issure)


        val issure = intent.getStringExtra("issure")


        tvCOntent.text =issure


    }
}
