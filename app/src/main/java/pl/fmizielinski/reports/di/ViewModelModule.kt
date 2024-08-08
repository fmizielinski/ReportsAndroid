package pl.fmizielinski.reports.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("pl.fmizielinski.reports.ui")
class ViewModelModule {

    @Single
    fun dispatcher(): CoroutineDispatcher = Dispatchers.Default
}
