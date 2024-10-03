package pl.fmizielinski.reports.data.network.report.model

import kotlinx.serialization.Serializable
import pl.fmizielinski.reports.data.network.serialization.LocalDateTimeAsString

@Serializable
data class ReportDetailsResponseModel(
    val id: Int,
    val title: String,
    val description: String,
    val reportDate: LocalDateTimeAsString,
    val userId: Int,
    val attachments: List<AttachmentModel>,
) {

    @Serializable
    data class AttachmentModel(
        val id: Int,
        val createDate: LocalDateTimeAsString,
    )
}
