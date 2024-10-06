package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.base.BaseUseCase
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.mapper.toReportDetails
import pl.fmizielinski.reports.domain.report.model.ReportDetails
import pl.fmizielinski.reports.domain.utils.PathProvider

@Factory
class GetReportDetailsUseCase(
    private val reportService: ReportService,
    private val dateFormatter: DateFormatter,
    private val pathProvider: PathProvider,
) : BaseUseCase() {

    suspend operator fun invoke(id: Int): ReportDetails {
        val response = catchHttpExceptions(
            body = { reportService.getReportDetails(id) },
            handler = { it.toErrorException() },
        )
        return response.toReportDetails(
            attachmentPath = pathProvider.getAttachmentPath(),
            dateFormatter = dateFormatter,
        )
    }

    override fun genericErrorException(cause: Throwable): SimpleErrorException {
        return SimpleErrorException(
            uiMessage = R.string.reportDetailsScreen_error_loading,
            message = "Report details loading error",
            cause = cause,
        )
    }
}
