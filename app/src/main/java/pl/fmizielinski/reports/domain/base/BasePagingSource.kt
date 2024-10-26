package pl.fmizielinski.reports.domain.base

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException

abstract class BasePagingSource<T : Any> : PagingSource<Int, T>() {

    abstract suspend fun loadData(page: Int, loadSize: Int): List<T>

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val data = loadData(params.page, params.loadSize)
            LoadResult.Page(
                data = data,
                prevKey = params.getPrevKey(),
                nextKey = params.getNextKey(data),
            )
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    private val LoadParams<Int>.page: Int
        get() = key ?: 1

    private fun LoadParams<Int>.getPrevKey() = if (page == 1) null else page - 1

    private fun LoadParams<Int>.getNextKey(data: List<T>): Int? {
        return when {
            data.isEmpty() || data.size < loadSize -> null

            else -> page + 1
        }
    }
}
