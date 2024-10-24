package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.report.model.AddCommentData
import java.time.LocalDateTime

fun addCommentData(
    comment: String = "content",
    createDate: LocalDateTime = LocalDateTime.now(),
) = AddCommentData(
    comment = comment,
    createDate = createDate,
)
