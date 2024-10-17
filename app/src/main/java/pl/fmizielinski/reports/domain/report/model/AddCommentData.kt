package pl.fmizielinski.reports.domain.report.model

import pl.fmizielinski.reports.data.network.report.model.AddCommentRequestModel
import java.time.LocalDateTime

data class AddCommentData(
    val comment: String,
    val createDate: LocalDateTime,
)

fun AddCommentData.toAddCommentRequest() = AddCommentRequestModel(
    comment = comment,
    createDate = createDate,
)
