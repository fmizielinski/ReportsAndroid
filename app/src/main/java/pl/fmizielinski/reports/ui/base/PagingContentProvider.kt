package pl.fmizielinski.reports.ui.base

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PagingContentProvider<Input : Any, Output : Any> {

    val pagingContent: Flow<PagingData<Output>>
        get() = providePagingContentFlow()
            .map(::mapPagingContent)

    fun providePagingContentFlow(): Flow<PagingData<Input>>

    fun mapPagingContent(data: PagingData<Input>): PagingData<Output>
}
