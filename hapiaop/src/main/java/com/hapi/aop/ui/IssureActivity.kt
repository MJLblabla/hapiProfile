package com.hapi.aop.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hapi.aop.Issure
import com.hapi.aop.R
import kotlinx.android.synthetic.main.activity_issure.*
import java.text.DecimalFormat


class IssureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issure)
        val issure = intent.getParcelableExtra<Issure>("issure")
        issure?.let {
            val msg = "msg  ${issure.msg}  " +
                    "\n availMemory ${issure.availMemory} " +
                    " \n totalMemory ${issure.totalMemory} " +
                    "\n foregroundPageName  ${issure.foregroundPageName}" +
                    "  cpuRate ${issure.cpuRate} \n\n"
            val sb = StringBuffer()
            sb.append(msg)
            issure.methodBeats?.forEachIndexed { index, methodBeat ->
                sb.append("top  ${index}  ${methodBeat.sign}  cost ${methodBeat.cost} \n")

            }
            tvCOntent.text = sb.toString()

        }

    }
}
