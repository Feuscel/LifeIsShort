package dev.feuscel.lifeisshort

import android.app.Application
import com.google.android.material.color.DynamicColors

class LifeIsShortApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Use the user's Material You wallpaper palette on Android 12 and later.
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
