package pl.fmizielinski.reports.domain.report.model

import java.io.File

interface AttachmentData {
    val localId: Int
    val file: File
}
