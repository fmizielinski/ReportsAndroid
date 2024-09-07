package pl.fmizielinski.reports.domain.mapper

import pl.fmizielinski.reports.data.network.report.model.ReportsResponseModel
import pl.fmizielinski.reports.domain.model.Report

// region reports

fun ReportsResponseModel.toReports(dataFormatter: DataFormatter) = reports.map {
    it.toReport(dataFormatter)
}

fun ReportsResponseModel.ReportModel.toReport(dataFormatter: DataFormatter) = Report(
    id = id,
    title = title,
    description = description,
    reportDate = dataFormatter.formatReportListDate(reportDate),
    comments = comments,
)

// endregion reports
