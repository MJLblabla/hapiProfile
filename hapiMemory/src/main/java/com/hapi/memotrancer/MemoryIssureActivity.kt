package com.hapi.memotrancer

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_memory_issure.*

class MemoryIssureActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_issure)
        val memoryIssure = intent.getStringExtra("memoryIssure")
        tvMemory.setText(memoryIssure)
    }
}