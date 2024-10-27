package pl.fmizielinski.reports.domain.report.paging

import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.base.BasePagingSource
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.mapper.toComments
import pl.fmizielinski.reports.domain.report.model.Comment

class CommentsPagingSource(
    private val reportId: Int,
    private val reportService: ReportService,
    private val dateFormatter: DateFormatter,
) : BasePagingSource<Comment>() {

    override suspend fun loadData(page: Int, loadSize: Int): List<Comment> {
        val response = reportService.getComments(reportId, page, loadSize)
        return response.toComments(dateFormatter)
    }
}
