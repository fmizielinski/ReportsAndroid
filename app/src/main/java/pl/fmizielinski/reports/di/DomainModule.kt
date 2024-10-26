package pl.fmizielinski.reports.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.report.paging.PagingSourceProvider
import pl.fmizielinski.reports.domain.report.paging.ReportsPagingSource

@Module
@ComponentScan("pl.fmizielinski.reports.domain")
class DomainModule {

    @Factory
    fun pagingSourceProvider(
        reportService: ReportService,
        dateFormatter: DateFormatter,
    ): PagingSourceProvider<ReportsPagingSource> {
        return object : PagingSourceProvider<ReportsPagingSource> {
            override fun providePagingSource(): ReportsPagingSource {
                return ReportsPagingSource(reportService, dateFormatter)
            }
        }
    }
}
