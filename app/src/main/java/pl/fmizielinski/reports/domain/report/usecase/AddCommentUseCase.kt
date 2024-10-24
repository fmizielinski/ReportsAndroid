package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.base.BaseUseCase
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.mapper.toComment
import pl.fmizielinski.reports.domain.report.model.AddCommentData
import pl.fmizielinski.reports.domain.report.model.Comment
import pl.fmizielinski.reports.domain.report.model.toAddCommentRequest

@Factory
class AddCommentUseCase(
    private val reportService: ReportService,
    private val dateFormatter: DateFormatter,
) : BaseUseCase() {

    suspend operator fun invoke(reportId: Int, data: AddCommentData): Comment {
        val request = data.toAddCommentRequest()
        val response = catchHttpExceptions(
            body = { reportService.addComment(reportId, request) },
            handler = { it.toErrorException() },
        )
        return response.toComment(dateFormatter)
    }
}