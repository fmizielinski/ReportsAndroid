package pl.fmizielinski.reports.fixtures.data

import pl.fmizielinski.reports.data.network.report.model.ReportsResponseModel
import java.time.LocalDateTime

fun reportsResponseModel(
    reports: List<ReportsResponseModel.ReportModel> = listOf(reportModel()),
) = ReportsResponseModel(reports = reports)

fun reportModel(
    id: Int = 1,
    title: String = "title",
    description: String = "description",
    reportDate: LocalDateTime = LocalDateTime.now(),
) = ReportsResponseModel.ReportModel(
    id = id,
    title = title,
    description = description,
    reportDate = reportDate,
)
