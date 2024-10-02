package pl.fmizielinski.reports.domain.usecase.report

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.domain.model.AttachmentData
import pl.fmizielinski.reports.ui.main.attachment.model.AttachmentGalleryNavArgs

@Factory
class GetAttachmentGalleryNavArgs {

    operator fun invoke(
        id: Int,
        attachments: List<AttachmentData>,
    ): AttachmentGalleryNavArgs {
        val index = attachments.indexOfFirst { it.localId == id }
        val files = attachments.map { it.file.absolutePath }
        return AttachmentGalleryNavArgs(
            initialIndex = index,
            attachments = ArrayList(files),
        )
    }
}
