package pl.fmizielinski.reports.domain.model

import java.io.File

interface AttachmentData {
    val localId: Int
    val file: File
}
