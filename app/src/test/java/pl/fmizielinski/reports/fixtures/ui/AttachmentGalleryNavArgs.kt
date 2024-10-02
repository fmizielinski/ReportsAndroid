package pl.fmizielinski.reports.fixtures.ui

import pl.fmizielinski.reports.ui.main.attachment.model.AttachmentGalleryNavArgs

fun attachmentGalleryNavArgs(
    initialIndex: Int = 0,
    attachments: List<String> = emptyList(),
) = AttachmentGalleryNavArgs(
    initialIndex = initialIndex,
    attachments = ArrayList(attachments),
)
