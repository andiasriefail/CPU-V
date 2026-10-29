package com.CPU.V.Vhynlain

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeHelper.apply(this)
    }
}
