package pl.fmizielinski.reports.domain.usecase.auth.report

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.mapper.DataFormatter
import pl.fmizielinski.reports.domain.mapper.toReports
import pl.fmizielinski.reports.domain.model.Report

@Factory
class GetReportsUseCase(
    private val reportService: ReportService,
    private val dateFormatter: DataFormatter,
) {

    suspend operator fun invoke(): List<Report> {
        val reportsResponseModel = reportService.getReports()
        return reportsResponseModel.toReports(dateFormatter)
    }
}
