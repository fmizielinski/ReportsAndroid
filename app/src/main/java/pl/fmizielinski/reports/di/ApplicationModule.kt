package pl.fmizielinski.reports.di

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import pl.fmizielinski.reports.utils.ApplicationConfig

@Module
class ApplicationModule {

    @Factory
    fun config(): ApplicationConfig = ApplicationConfig()
}
