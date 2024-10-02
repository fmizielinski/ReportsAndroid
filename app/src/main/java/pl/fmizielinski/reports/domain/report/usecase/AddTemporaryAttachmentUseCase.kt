package pl.fmizielinski.reports.domain.report.usecase

import okhttp3.MultipartBody
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.report.model.AddTemporaryAttachmentData
import pl.fmizielinski.reports.domain.report.model.TemporaryAttachmentUploadResult
import pl.fmizielinski.reports.domain.report.model.TemporaryAttachmentUploadResult.Complete
import pl.fmizielinski.reports.domain.report.model.TemporaryAttachmentUploadResult.Progress

@Factory
class AddTemporaryAttachmentUseCase(
    private val reportService: ReportService,
) : BaseAddAttachmentUseCase<AddTemporaryAttachmentData, TemporaryAttachmentUploadResult>() {

    override fun getProgressResult(progress: Float) = Progress(progress)

    override suspend fun getCompleteResult(
        data: AddTemporaryAttachmentData,
        filePart: MultipartBody.Part,
    ): TemporaryAttachmentUploadResult {
        val response = catchHttpExceptions(
            body = { reportService.addTemporaryAttachment(filePart) },
            handler = { it.toErrorException() },
        )
        return Complete(response.uuid)
    }
}
