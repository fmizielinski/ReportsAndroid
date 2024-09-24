package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.model.TemporaryAttachmentUploadResult

fun progressTemporaryAttachmentUploadResult(
    progress: Float,
) = TemporaryAttachmentUploadResult.Progress(
    progress = progress,
)

fun completeTemporaryAttachmentUploadResult(
    uuid: String,
) = TemporaryAttachmentUploadResult.Complete(
    uuid = uuid,
)
