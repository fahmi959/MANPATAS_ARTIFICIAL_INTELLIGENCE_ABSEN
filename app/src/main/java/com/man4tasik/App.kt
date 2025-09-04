package com.man4tasik

import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.man4tasik.BuildConfig

import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)


    }

}
