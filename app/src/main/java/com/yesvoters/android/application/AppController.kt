package com.yesvoters.android.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.yesvoters.android.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Singleton

@Singleton
@HiltAndroidApp
class AppController : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        var DEBUG: Boolean = false
        private lateinit var appContext: Context
        var currentActivity: Activity? = null

        fun getGlobalContext(): Context = appContext
    }

    override fun onCreate() {
        super.onCreate()
        DEBUG = BuildConfig.DEBUG
        appContext = applicationContext
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity === activity) {
            currentActivity = null
        }
    }

    // Other lifecycle methods (empty for now)
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    override fun onTerminate() {
        unregisterActivityLifecycleCallbacks(this)
        super.onTerminate()
    }
}
