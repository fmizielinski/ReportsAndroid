package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.domain.base.BasePagingUseCase
import pl.fmizielinski.reports.domain.report.model.Report
import pl.fmizielinski.reports.domain.report.paging.PagingSourceProvider
import pl.fmizielinski.reports.domain.report.paging.ReportsPagingSource
import pl.fmizielinski.reports.utils.ApplicationConfig

@Factory
class GetReportsUseCase(
    config: ApplicationConfig,
    pagingSourceProvider: PagingSourceProvider<ReportsPagingSource>,
) : BasePagingUseCase<ReportsPagingSource, Report>(
    config = config,
    pagingSourceProvider = pagingSourceProvider,
)
