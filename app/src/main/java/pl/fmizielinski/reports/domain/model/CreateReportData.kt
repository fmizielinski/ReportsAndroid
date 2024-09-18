package pl.fmizielinski.reports.domain.model

import pl.fmizielinski.reports.data.network.report.model.CreateReportRequest
import java.time.LocalDateTime

data class CreateReportData(
    val title: String,
    val description: String,
    val reportDate: LocalDateTime,
    val attachments: List<String>,
)

fun CreateReportData.toCreateReportRequest() = CreateReportRequest(
    title = title,
    description = description,
    reportDate = reportDate,
    attachments = attachments,
)
