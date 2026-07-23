package com.example.mindquest

import android.app.Application
import com.example.mindquest.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MindQuestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MindQuestApplication)
            modules(appModules)
        }
    }
}
