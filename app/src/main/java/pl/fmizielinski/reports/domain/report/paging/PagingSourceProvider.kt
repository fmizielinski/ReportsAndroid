package pl.fmizielinski.reports.domain.report.paging

import pl.fmizielinski.reports.domain.base.BasePagingSource

interface PagingSourceProvider<Input, Output : BasePagingSource<*>> {

    fun providePagingSource(data: Input): Output
}
