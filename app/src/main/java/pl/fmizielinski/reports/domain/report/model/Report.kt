package pl.fmizielinski.reports.domain.report.model

data class Report(
    val id: Int,
    val title: String,
    val description: String,
    val reportDate: String,
    val comments: Int,
)
