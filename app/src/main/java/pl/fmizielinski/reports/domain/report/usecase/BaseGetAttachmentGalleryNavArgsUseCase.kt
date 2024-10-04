package pl.fmizielinski.reports.domain.report.usecase

import pl.fmizielinski.reports.ui.main.attachment.model.AttachmentGalleryNavArgs

abstract class BaseGetAttachmentGalleryNavArgsUseCase<T> {

    operator fun invoke(
        id: Int,
        attachments: List<T>,
    ): AttachmentGalleryNavArgs {
        val index = attachments.indexOfFirst { it.getId() == id }
        val paths = attachments.map { it.getPath() }
        return AttachmentGalleryNavArgs(
            initialIndex = index,
            attachments = ArrayList(paths),
        )
    }

    abstract fun T.getId(): Int

    abstract fun T.getPath(): String


}
