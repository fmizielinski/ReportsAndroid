package pl.fmizielinski.reports.data.network.report.model

import kotlinx.serialization.Serializable
import pl.fmizielinski.reports.data.network.serialization.LocalDateTimeAsString

@Serializable
data class CreateReportRequest(
    val title: String,
    val description: String,
    val reportDate: LocalDateTimeAsString,
)