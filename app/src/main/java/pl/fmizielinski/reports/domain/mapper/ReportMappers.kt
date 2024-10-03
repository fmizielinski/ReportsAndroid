package pl.fmizielinski.reports.domain.mapper

import pl.fmizielinski.reports.data.network.report.model.ReportDetailsResponseModel
import pl.fmizielinski.reports.data.network.report.model.ReportsResponseModel
import pl.fmizielinski.reports.domain.report.model.Report
import pl.fmizielinski.reports.domain.report.model.ReportDetails
import java.time.LocalDateTime

fun ReportsResponseModel.toReports(dateFormatter: DateFormatter) = reports.map {
    it.toReport(dateFormatter)
}

fun ReportsResponseModel.ReportModel.toReport(dateFormatter: DateFormatter): Report {
    val currentDate = LocalDateTime.now()
    val isCurrentYear = reportDate.year == currentDate.year
    return Report(
        id = id,
        title = title,
        description = description,
        reportDate = dateFormatter.formatReportListDate(reportDate, withYear = !isCurrentYear),
        comments = comments,
    )
}

fun ReportDetailsResponseModel.toReportDetails(
    attachmentPath: String,
    dateFormatter: DateFormatter,
) = ReportDetails(
    id = id,
    title = title,
    description = description,
    reportDate = dateFormatter.formatReportDetailsDate(reportDate),
    attachments = attachments.map { "$attachmentPath${it.id}" },
)
