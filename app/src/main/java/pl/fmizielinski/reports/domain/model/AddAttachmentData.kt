package pl.fmizielinski.reports.domain.model

import java.io.File

interface AddAttachmentData {
    val file: File
}

data class AddReportAttachmentData(
    val reportId: Int,
    override val file: File,
) : AddAttachmentData

data class AddTemporaryAttachmentData(
    override val file: File,
) : AddAttachmentData
