package pl.fmizielinski.reports.domain.report.model

data class Comment(
    val id: Int,
    val comment: String,
    val user: String,
    val createDate: String,
    val isMine: Boolean,
)
