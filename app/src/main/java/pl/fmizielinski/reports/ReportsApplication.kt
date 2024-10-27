package pl.fmizielinski.reports

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.module
import pl.fmizielinski.reports.di.ApplicationModule
import pl.fmizielinski.reports.di.DatabaseModule
import pl.fmizielinski.reports.di.DomainModule
import pl.fmizielinski.reports.di.NetworkModule
import pl.fmizielinski.reports.di.ViewModelModule
import timber.log.Timber

class ReportsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            val appModule = module {
                single { koin }
            }
            androidLogger()
            androidContext(this@ReportsApplication)
            modules(
                appModule,
                ApplicationModule().module,
                NetworkModule().module,
                ViewModelModule().module,
                DomainModule().module,
                DatabaseModule().module,
            )
        }
    }
}
