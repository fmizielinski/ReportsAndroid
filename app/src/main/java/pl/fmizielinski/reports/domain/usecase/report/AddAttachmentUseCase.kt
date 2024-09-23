package pl.fmizielinski.reports.domain.usecase.report

import okhttp3.MultipartBody
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.model.AddAttachmentData
import pl.fmizielinski.reports.domain.model.AttachmentUploadResult
import pl.fmizielinski.reports.domain.model.AttachmentUploadResult.Complete
import pl.fmizielinski.reports.domain.model.AttachmentUploadResult.Progress

@Factory
class AddAttachmentUseCase(
    private val reportService: ReportService,
) : BaseAddAttachmentUseCase<AddAttachmentData, AttachmentUploadResult>() {

    override fun getProgressResult(progress: Float) = Progress(progress)

    override suspend fun getCompleteResult(
        data: AddAttachmentData,
        filePart: MultipartBody.Part,
    ): AttachmentUploadResult {
        catchHttpExceptions(
            body = { reportService.addAttachment(data.reportId, filePart) },
            handler = { it.toErrorException() },
        )
        return Complete
    }
}
