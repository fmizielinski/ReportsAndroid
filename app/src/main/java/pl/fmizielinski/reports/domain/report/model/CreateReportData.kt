package pl.fmizielinski.reports.domain.report.model

import pl.fmizielinski.reports.data.network.report.model.CreateReportRequestModel
import java.time.LocalDateTime

data class CreateReportData(
    val title: String,
    val description: String,
    val reportDate: LocalDateTime,
    val attachments: List<String>,
)

fun CreateReportData.toCreateReportRequest() = CreateReportRequestModel(
    title = title,
    description = description,
    reportDate = reportDate,
    attachments = attachments,
)
