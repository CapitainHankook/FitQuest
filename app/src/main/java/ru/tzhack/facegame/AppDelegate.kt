package ru.tzhack.facegame

import android.app.Application
import ru.tzhack.facegame.facetraking.mlkit.MlKitEngine

class AppDelegate : Application() {

    companion object {

        private lateinit var instance: AppDelegate

        fun get() = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        MlKitEngine.initMlKit()
    }
}