package pl.fmizielinski.reports.domain.usecase.report

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.error.ErrorException
import java.io.File

@Factory
class AddTemporaryAttachmentUseCase(
    private val reportService: ReportService,
) : BaseAddAttachmentUseCase() {

    @Throws(ErrorException::class)
    suspend operator fun invoke(file: File) {
        catchHttpExceptions(
            body = { reportService.addTemporaryAttachment(createFilePart(file)) },
            handler = { it.toErrorException() },
        )
    }
}
