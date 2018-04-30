package sakura.felica

import android.app.Application
import timber.log.Timber

class FelicaApp : Application() {

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }
}
