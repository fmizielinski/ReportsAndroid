package pl.fmizielinski.reports.ui.base

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

interface PagingContentProvider<T : Any> {

    val pagingContent: Flow<PagingData<T>>
        get() = providePagingContentFlow()
            .distinctUntilChanged()

    fun providePagingContentFlow(): Flow<PagingData<T>>

}
