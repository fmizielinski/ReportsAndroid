package pl.fmizielinski.reports.domain.report.paging

import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.base.BasePagingSource
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.mapper.toReports
import pl.fmizielinski.reports.domain.report.model.Report

class ReportsPagingSource(
    private val reportService: ReportService,
    private val dateFormatter: DateFormatter,
) : BasePagingSource<Report>() {

    override suspend fun loadData(page: Int, loadSize: Int): List<Report> {
        val response = reportService.getReports(page, loadSize)
        return response.toReports(dateFormatter)
    }
}
