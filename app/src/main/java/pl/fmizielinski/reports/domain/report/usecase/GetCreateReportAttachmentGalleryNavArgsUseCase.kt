package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.domain.report.model.AttachmentData
import pl.fmizielinski.reports.ui.main.attachment.model.AttachmentGalleryNavArgs

@Factory
class GetCreateReportAttachmentGalleryNavArgsUseCase
    : BaseGetAttachmentGalleryNavArgsUseCase<AttachmentData>() {

    override fun AttachmentData.getId(): Int = localId

    override fun AttachmentData.getPath(): String = file.absolutePath
}
