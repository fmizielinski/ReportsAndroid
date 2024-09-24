package pl.fmizielinski.reports.domain.mapper

import pl.fmizielinski.reports.data.network.report.model.ReportsResponseModel
import pl.fmizielinski.reports.domain.model.Report

// region reports

fun ReportsResponseModel.toReports(dateFormatter: DateFormatter) = reports.map {
    it.toReport(dateFormatter)
}

fun ReportsResponseModel.ReportModel.toReport(dateFormatter: DateFormatter) = Report(
    id = id,
    title = title,
    description = description,
    reportDate = dateFormatter.formatReportListDate(reportDate),
    comments = comments,
)

// endregion reports
