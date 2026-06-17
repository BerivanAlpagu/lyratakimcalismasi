package com.turkcell.lyraapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt'in bağımlılık grafını başlattığı uygulama giriş noktası.
 *
 * `@HiltAndroidApp` annotasyonu, derleme zamanında uygulama düzeyindeki bileşeni üretir;
 * bu sınıf [AndroidManifest] içinde `android:name` ile tanımlanmadan Hilt çalışmaz.
 */
@HiltAndroidApp
class LyraApplication : Application()
