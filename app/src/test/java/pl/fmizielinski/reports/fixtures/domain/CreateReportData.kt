package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.model.CreateReportData
import java.time.LocalDateTime

fun createReportData(
    title: String = "title",
    description: String = "description",
    reportDate: LocalDateTime = LocalDateTime.now(),
) = CreateReportData(
    title = title,
    description = description,
    reportDate = reportDate,
)
