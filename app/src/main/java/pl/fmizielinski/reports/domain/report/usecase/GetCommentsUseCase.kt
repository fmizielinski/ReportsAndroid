package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.base.BaseUseCase
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.mapper.toComments
import pl.fmizielinski.reports.domain.report.model.Comment

@Factory
class GetCommentsUseCase(
    private val reportService: ReportService,
    private val dateFormatter: DateFormatter,
) : BaseUseCase() {

    suspend operator fun invoke(reportId: Int): List<Comment> {
        val response = catchHttpExceptions(
            body = { reportService.getComments(reportId) },
            handler = { it.toErrorException() },
        )
        return response.toComments(dateFormatter)
    }

    override fun genericErrorException(cause: Throwable): SimpleErrorException {
        return SimpleErrorException(
            uiMessage = R.string.reportDetailsScreen_error_commentsLoading,
            message = "Comments loading error",
            cause = cause,
        )
    }
}
