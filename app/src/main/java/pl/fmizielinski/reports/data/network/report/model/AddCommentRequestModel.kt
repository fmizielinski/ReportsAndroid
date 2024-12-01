package pl.fmizielinski.reports.data.network.report.model

import kotlinx.serialization.Serializable
import pl.fmizielinski.reports.data.network.serialization.LocalDateTimeAsString

@Serializable
data class AddCommentRequestModel(
    val comment: String,
    val createDate: LocalDateTimeAsString,
)
