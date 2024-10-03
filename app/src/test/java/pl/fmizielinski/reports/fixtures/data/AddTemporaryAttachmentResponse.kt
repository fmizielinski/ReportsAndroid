package pl.fmizielinski.reports.fixtures.data

import pl.fmizielinski.reports.data.network.report.model.AddTemporaryAttachmentResponseModel

fun addTemporaryAttachmentResponse(
    uuid: String = "uuid",
) = AddTemporaryAttachmentResponseModel(
    uuid = uuid,
)
