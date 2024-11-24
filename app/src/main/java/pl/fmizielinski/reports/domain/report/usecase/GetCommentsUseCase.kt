package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Named
import pl.fmizielinski.reports.di.DomainModule.Companion.COMMENTS_PAGING_SOURCE_PROVIDER
import pl.fmizielinski.reports.domain.base.BasePagingUseCase
import pl.fmizielinski.reports.domain.report.model.Comment
import pl.fmizielinski.reports.domain.report.paging.CommentsPagingSource
import pl.fmizielinski.reports.domain.report.paging.PagingSourceProvider
import pl.fmizielinski.reports.utils.ApplicationConfig

@Factory
class GetCommentsUseCase(
    @InjectedParam reportId: Int,
    config: ApplicationConfig,
    @Named(COMMENTS_PAGING_SOURCE_PROVIDER)
    sourceProvider: PagingSourceProvider<Int, CommentsPagingSource>,
) : BasePagingUseCase<CommentsPagingSource, Int, Comment>(
    inputData = reportId,
    config = config,
    pagingSourceProvider = sourceProvider,
)
