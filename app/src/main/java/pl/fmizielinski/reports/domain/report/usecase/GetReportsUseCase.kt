package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import pl.fmizielinski.reports.di.DomainModule.Companion.REPORTS_PAGING_SOURCE_PROVIDER
import pl.fmizielinski.reports.domain.base.BasePagingUseCase
import pl.fmizielinski.reports.domain.report.model.Report
import pl.fmizielinski.reports.domain.report.paging.PagingSourceProvider
import pl.fmizielinski.reports.domain.report.paging.ReportsPagingSource
import pl.fmizielinski.reports.utils.ApplicationConfig

@Factory
class GetReportsUseCase(
    config: ApplicationConfig,
    @Named(REPORTS_PAGING_SOURCE_PROVIDER)
    sourceProvider: PagingSourceProvider<Unit, ReportsPagingSource>,
) : BasePagingUseCase<ReportsPagingSource, Unit, Report>(
    inputData = Unit,
    config = config,
    pagingSourceProvider = sourceProvider,
)
