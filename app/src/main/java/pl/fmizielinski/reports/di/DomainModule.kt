package pl.fmizielinski.reports.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.report.paging.CommentsPagingSource
import pl.fmizielinski.reports.domain.report.paging.PagingSourceProvider
import pl.fmizielinski.reports.domain.report.paging.ReportsPagingSource

@Module
@ComponentScan("pl.fmizielinski.reports.domain")
class DomainModule {

    @Factory
    @Named(REPORTS_PAGING_SOURCE_PROVIDER)
    fun reportsPagingSourceProvider(
        reportService: ReportService,
        dateFormatter: DateFormatter,
    ): PagingSourceProvider<Unit, ReportsPagingSource> {
        return object : PagingSourceProvider<Unit, ReportsPagingSource> {
            override fun providePagingSource(data: Unit): ReportsPagingSource {
                return ReportsPagingSource(reportService, dateFormatter)
            }
        }
    }

    @Factory
    @Named(COMMENTS_PAGING_SOURCE_PROVIDER)
    fun commentsPagingSourceProvider(
        reportService: ReportService,
        dateFormatter: DateFormatter,
    ): PagingSourceProvider<Int, CommentsPagingSource> {
        return object : PagingSourceProvider<Int, CommentsPagingSource> {
            override fun providePagingSource(data: Int): CommentsPagingSource {
                return CommentsPagingSource(
                    reportId = data,
                    reportService = reportService,
                    dateFormatter = dateFormatter,
                )
            }
        }
    }

    companion object {
        const val REPORTS_PAGING_SOURCE_PROVIDER = "reportsPagingSourceProvider"
        const val COMMENTS_PAGING_SOURCE_PROVIDER = "commentsPagingSourceProvider"
    }
}
