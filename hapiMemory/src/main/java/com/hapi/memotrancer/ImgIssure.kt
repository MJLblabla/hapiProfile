package com.hapi.memotrancer

import android.content.Intent
import com.hapi.baseprofile.ActivityCollection
import com.hapi.baseprofile.NotificationAble

class ImgIssure : NotificationAble {

    var bitmapWidth = 0
    var bitmapHeight =0
    var stack = ""

    override fun getTittle(): String {
       return "老铁 你这张图贼大"
    }

    override fun getContent(): String {
      return stack
    }

    override fun getIntent(): Intent? {
        return null
    }

}