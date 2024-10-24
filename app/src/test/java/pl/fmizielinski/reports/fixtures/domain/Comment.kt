package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.report.model.Comment

fun comment(
    id: Int = 1,
    comment: String = "comment",
    user: String = "user",
    createDate: String = "12 Jun",
    isMine: Boolean = false,
) = Comment(
    id = id,
    comment = comment,
    user = user,
    createDate = createDate,
    isMine = isMine,
)
