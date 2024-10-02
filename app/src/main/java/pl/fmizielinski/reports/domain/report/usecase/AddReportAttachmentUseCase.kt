package pl.fmizielinski.reports.domain.report.usecase

import okhttp3.MultipartBody
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.report.model.AddReportAttachmentData
import pl.fmizielinski.reports.domain.report.model.AttachmentUploadResult
import pl.fmizielinski.reports.domain.report.model.AttachmentUploadResult.Complete
import pl.fmizielinski.reports.domain.report.model.AttachmentUploadResult.Progress

@Factory
class AddReportAttachmentUseCase(
    private val reportService: ReportService,
) : BaseAddAttachmentUseCase<AddReportAttachmentData, AttachmentUploadResult>() {

    override fun getProgressResult(progress: Float) = Progress(progress)

    override suspend fun getCompleteResult(
        data: AddReportAttachmentData,
        filePart: MultipartBody.Part,
    ): AttachmentUploadResult {
        catchHttpExceptions(
            body = { reportService.addAttachment(data.reportId, filePart) },
            handler = { it.toErrorException() },
        )
        return Complete
    }
}
