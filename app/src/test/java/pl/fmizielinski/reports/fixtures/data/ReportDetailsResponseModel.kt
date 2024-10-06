package pl.fmizielinski.reports.fixtures.data

import pl.fmizielinski.reports.data.network.report.model.ReportDetailsResponseModel
import java.time.LocalDateTime

fun reportDetailsResponseModel(
    id: Int = 1,
    title: String = "title",
    description: String = "description",
    reportDate: LocalDateTime = LocalDateTime.now(),
    userId: Int = 2,
    attachments: List<ReportDetailsResponseModel.AttachmentModel> = emptyList(),
) = ReportDetailsResponseModel(
    id = id,
    title = title,
    description = description,
    reportDate = reportDate,
    userId = userId,
    attachments = attachments,
)

fun attachmentModel(
    id: Int = 1,
    createDate: LocalDateTime = LocalDateTime.now(),
) = ReportDetailsResponseModel.AttachmentModel(
    id = id,
    createDate = createDate,
)
