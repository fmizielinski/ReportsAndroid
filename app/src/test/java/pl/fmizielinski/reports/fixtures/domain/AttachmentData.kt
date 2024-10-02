package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.model.AttachmentData
import java.io.File

fun attachmentData(
    localId: Int = 0,
    file: File = File(""),
) = object : AttachmentData {
    override val localId = localId
    override val file = file
}
