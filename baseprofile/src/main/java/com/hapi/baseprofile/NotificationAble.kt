package com.hapi.baseprofile

import android.content.Intent

interface NotificationAble {

    fun getTittle():String
    fun getContent():String
    fun getIntent(): Intent?

}