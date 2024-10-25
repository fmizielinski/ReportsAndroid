package pl.fmizielinski.reports.domain.base

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import pl.fmizielinski.reports.domain.report.paging.PagingSourceProvider
import pl.fmizielinski.reports.utils.ApplicationConfig
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

abstract class BasePagingUseCase<Source : BasePagingSource<Output>, Output : Any>(
    private val config: ApplicationConfig,
    private val pagingSourceProvider: PagingSourceProvider<Source>,
) : BaseUseCase() {

    val data: Flow<PagingData<Output>>
        get() = pager.flow

    private var pagingSource = Optional.empty<Source>()

    private val pager: Pager<Int, Output> by lazy {
        Pager(
            config = PagingConfig(
                pageSize = config.reportListPageSize,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                pagingSourceProvider.providePagingSource().also { pagingSource = Optional.of(it) }
            },
        )
    }

    operator fun invoke() {
        pagingSource.getOrNull()?.invalidate()
    }
}
