package com.hapi.mylibrary

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hapi.base_mvvm.activity.BaseFrameActivity
import com.hapi.base_mvvm.mvvm.BaseVmActivity

class EmpActivity : BaseFrameActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_emp;
    }

    override fun init() {
        Thread.sleep(2000)
    }

    override fun showLoading(toShow: Boolean) {
    }

    override fun isToolBarEnable(): Boolean {
        return false
    }
}