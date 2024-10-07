package pl.fmizielinski.reports.data.network.report.model

import kotlinx.serialization.Serializable
import pl.fmizielinski.reports.data.network.serialization.LocalDateTimeAsString

@Serializable
data class CommentsResponseModel(
    val comments: List<CommentModel>,
) {

    @Serializable
    data class CommentModel(
        val id: Int,
        val comment: String,
        val user: String,
        val createDate: LocalDateTimeAsString,
        val isMine: Boolean,
    )
}
