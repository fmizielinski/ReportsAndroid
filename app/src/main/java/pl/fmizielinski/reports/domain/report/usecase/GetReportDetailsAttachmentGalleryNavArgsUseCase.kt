package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.domain.report.model.AttachmentData
import pl.fmizielinski.reports.domain.report.model.ReportDetails
import pl.fmizielinski.reports.ui.main.attachment.model.AttachmentGalleryNavArgs

@Factory
class GetReportDetailsAttachmentGalleryNavArgsUseCase
    : BaseGetAttachmentGalleryNavArgsUseCase<ReportDetails.Attachment>() {

    override fun ReportDetails.Attachment.getId(): Int = id

    override fun ReportDetails.Attachment.getPath(): String = path
}