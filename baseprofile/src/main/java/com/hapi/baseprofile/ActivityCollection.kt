package com.hapi.baseprofile

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle

object ActivityCollection {

    var currentActivity: Activity? = null
        private set
    var appContext: Context? = null

    private
    var isAppForeground = false
        private set


    private var isInit = false
    fun init(application: Application) {
        appContext = application
        if (isInit) {
            return
        }
        isInit = true
        application.registerActivityLifecycleCallbacks(callBack)
    }

    private val callBack = object : Application.ActivityLifecycleCallbacks {


        /**
         * Called when the Activity calls [super.onPause()][Activity.onPause].
         */
        override fun onActivityPaused(activity: Activity) {
            isAppForeground = false
        }

        /**
         * Called when the Activity calls [super.onStart()][Activity.onStart].
         */
        override fun onActivityStarted(activity: Activity) {
        }

        /**
         * Called when the Activity calls [super.onDestroy()][Activity.onDestroy].
         */
        override fun onActivityDestroyed(activity: Activity) {
            currentActivity = null
        }

        /**
         * Called when the Activity calls
         * [super.onSaveInstanceState()][Activity.onSaveInstanceState].
         */
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        /**
         * Called when the Activity calls [super.onStop()][Activity.onStop].
         */
        override fun onActivityStopped(activity: Activity) {}

        /**
         * Called when the Activity calls [super.onCreate()][Activity.onCreate].
         */
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        /**
         * Called when the Activity calls [super.onResume()][Activity.onResume].
         */
        override fun onActivityResumed(activity: Activity) {
            currentActivity = activity
            isAppForeground = true
        }
    }
}