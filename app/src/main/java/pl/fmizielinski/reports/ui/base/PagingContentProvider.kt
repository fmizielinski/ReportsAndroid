package pl.fmizielinski.reports.ui.base

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface PagingContentProvider<T : Any> {

    val pagingContent: Flow<PagingData<T>>
        get() = providePagingContentFlow()

    fun providePagingContentFlow(): Flow<PagingData<T>>

}
