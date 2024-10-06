package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.domain.report.model.AttachmentData

@Factory
class GetCreateReportAttachmentGalleryNavArgsUseCase
    : BaseGetAttachmentGalleryNavArgsUseCase<AttachmentData>() {

    override fun AttachmentData.getId(): Int = localId

    override fun AttachmentData.getPath(): String = file.absolutePath
}
