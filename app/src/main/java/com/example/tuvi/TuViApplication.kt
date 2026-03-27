package com.example.tuvi

import android.app.Application
import com.example.tuvi.di.AppContainer

class TuViApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
