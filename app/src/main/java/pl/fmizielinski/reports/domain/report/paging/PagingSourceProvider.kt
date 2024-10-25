package pl.fmizielinski.reports.domain.report.paging

import pl.fmizielinski.reports.domain.base.BasePagingSource

interface PagingSourceProvider<T : BasePagingSource<*>> {

    fun providePagingSource(): T
}
