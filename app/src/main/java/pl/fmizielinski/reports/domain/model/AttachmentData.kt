package pl.fmizielinski.reports.domain.model

import java.io.File

interface AttachmentData {
    val file: File
}

data class AddAttachmentData(
    val reportId: Int,
    override val file: File,
) : AttachmentData

data class AddTemporaryAttachmentData(
    override val file: File,
) : AttachmentData
