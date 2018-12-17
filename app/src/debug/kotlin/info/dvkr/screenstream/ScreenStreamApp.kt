package info.dvkr.screenstream

import android.app.Application
import android.os.StrictMode
import com.squareup.leakcanary.LeakCanary
import info.dvkr.screenstream.di.baseKoinModule
import info.dvkr.screenstream.service.AppService
import org.koin.android.ext.android.startKoin
import org.koin.log.Logger
import timber.log.Timber

class ScreenStreamApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
            Timber.e(throwable, "Uncaught throwable in thread ${thread.name}")
            defaultHandler.uncaughtException(thread, throwable)
        }

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .permitDiskReads()
                .permitDiskWrites()
                .penaltyLog()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        if (LeakCanary.isInAnalyzerProcess(this)) return
        LeakCanary.install(this)

        startKoin(this,
            listOf(baseKoinModule),
            logger = object : Logger {
                override fun debug(msg: String) = Timber.tag("Koin").d(msg)
                override fun err(msg: String) = Timber.tag("Koin").e(msg)
                override fun info(msg: String) = Timber.tag("Koin").i(msg)
            })

        AppService.startForegroundService(this)
    }
}