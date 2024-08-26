package pl.fmizielinski.reports.data.network.report.model

import kotlinx.serialization.Serializable
import pl.fmizielinski.reports.data.network.serialization.LocalDateTimeAsString

@Serializable
data class ReportsResponseModel(
    val reports: List<ReportModel>,
) {

    @Serializable
    data class ReportModel(
        val id: Int,
        val title: String,
        val description: String,
        val reportDate: LocalDateTimeAsString,
    )
}
