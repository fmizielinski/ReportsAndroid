package pl.fmizielinski.reports.domain.report.usecase

import kotlinx.coroutines.delay
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.base.BaseUseCase
import pl.fmizielinski.reports.domain.report.model.AddCommentData
import pl.fmizielinski.reports.domain.report.model.toAddCommentRequest

@Factory
class AddCommentUseCase(
    private val reportService: ReportService,
) : BaseUseCase() {

    suspend operator fun invoke(reportId: Int, data: AddCommentData) {
        val request = data.toAddCommentRequest()
        delay(10000L)
        catchHttpExceptions(
            body = { reportService.addComment(reportId, request) },
            handler = { it.toErrorException() },
        )
    }
}
