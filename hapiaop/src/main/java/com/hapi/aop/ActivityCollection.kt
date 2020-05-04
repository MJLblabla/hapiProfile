package com.hapi.aop

import android.app.Activity
import android.app.Application
import android.os.Bundle

object ActivityCollection {

    var currentActivity: Activity? = null
        private set


    val callBack = object : Application.ActivityLifecycleCallbacks {


        /**
         * Called when the Activity calls [super.onPause()][Activity.onPause].
         */
        override fun onActivityPaused(activity: Activity) {

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
        }
    }
}