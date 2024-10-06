package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.report.model.ReportDetails

fun reportDetails(
    id: Int = 1,
    title: String = "title",
    description: String = "description",
    reportDate: String = "reportDate",
    attachments: List<ReportDetails.Attachment> = emptyList(),
) = ReportDetails(
    id = id,
    title = title,
    description = description,
    reportDate = reportDate,
    attachments = attachments,
)

fun reportDetailsAttachment(
    id: Int = 1,
    path: String = "path",
) = ReportDetails.Attachment(
    id = id,
    path = path,
)
