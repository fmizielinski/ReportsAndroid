package pl.fmizielinski.reports.domain.report.model

data class ReportDetails(
    val id: Int,
    val title: String,
    val description: String,
    val reportDate: String,
    val attachments: List<String>,
)
