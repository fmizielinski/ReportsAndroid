package pl.fmizielinski.reports.fixtures.data

import pl.fmizielinski.reports.data.network.report.model.CommentsResponseModel
import java.time.LocalDateTime

fun commentsResponseModel(
    vararg comments: CommentsResponseModel.CommentModel,
) = CommentsResponseModel(
    comments = comments.toList(),
)

fun commentModel(
    id: Int = 1,
    comment: String = "comment",
    user: String = "user",
    createDate: LocalDateTime = LocalDateTime.now(),
    isMine: Boolean = true,
) = CommentsResponseModel.CommentModel(
    id = id,
    comment = comment,
    user = user,
    createDate = createDate,
    isMine = isMine,
)
