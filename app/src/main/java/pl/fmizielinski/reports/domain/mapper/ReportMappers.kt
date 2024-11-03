package pl.fmizielinski.reports.domain.mapper

import pl.fmizielinski.reports.data.network.report.model.CommentsResponseModel
import pl.fmizielinski.reports.data.network.report.model.ReportDetailsResponseModel
import pl.fmizielinski.reports.data.network.report.model.ReportsResponseModel
import pl.fmizielinski.reports.domain.report.model.Comment
import pl.fmizielinski.reports.domain.report.model.Report
import pl.fmizielinski.reports.domain.report.model.ReportDetails
import java.time.LocalDateTime

fun ReportsResponseModel.toReports(dateFormatter: DateFormatter) = reports.map {
    it.toReport(dateFormatter)
}

fun ReportsResponseModel.ReportModel.toReport(dateFormatter: DateFormatter): Report {
    val currentDate = LocalDateTime.now()
    val isCurrentYear = reportDate.year == currentDate.year
    return Report(
        id = id,
        title = title,
        description = description,
        reportDate = dateFormatter.formatReportListDate(reportDate, isCurrentYear),
        comments = comments,
    )
}

fun ReportDetailsResponseModel.toReportDetails(
    attachmentPath: String,
    dateFormatter: DateFormatter,
) = ReportDetails(
    id = id,
    title = title,
    description = description,
    reportDate = dateFormatter.formatReportDetailsDate(reportDate),
    attachments = attachments.map { it.toAttachment(attachmentPath) },
)

fun ReportDetailsResponseModel.AttachmentModel.toAttachment(
    attachmentPath: String,
) = ReportDetails.Attachment(
    id = id,
    path = "$attachmentPath$id",
)

fun CommentsResponseModel.toComments(dateFormatter: DateFormatter) = comments.map {
    it.toComment(dateFormatter)
}

fun CommentsResponseModel.CommentModel.toComment(
    dateFormatter: DateFormatter,
): Comment {
    val now = LocalDateTime.now()
    val isCurrentYear = createDate.year == now.year
    val date = dateFormatter.formatCommentDate(
        date = createDate,
        isToday = createDate.dayOfYear == now.dayOfYear && isCurrentYear,
        isCurrentWeek = now.dayOfYear - createDate.dayOfYear < 7 && isCurrentYear,
        isCurrentYear = createDate.year == now.year,
    )
    return Comment(
        id = id,
        comment = comment,
        user = user,
        createDate = date,
        isMine = isMine,
    )
}
