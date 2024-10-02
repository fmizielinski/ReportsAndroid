package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.report.model.CreateReportData
import java.time.LocalDateTime

fun createReportData(
    title: String = "title",
    description: String = "description",
    reportDate: LocalDateTime = LocalDateTime.now(),
    attachments: List<String> = emptyList(),
) = CreateReportData(
    title = title,
    description = description,
    reportDate = reportDate,
    attachments = attachments,
)
