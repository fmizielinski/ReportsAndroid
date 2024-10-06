package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.base.BaseUseCase
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.UnauthorizedErrorException
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.mapper.toReports
import pl.fmizielinski.reports.domain.report.model.Report
import retrofit2.HttpException

@Factory
class GetReportsUseCase(
    private val reportService: ReportService,
    private val dateFormatter: DateFormatter,
) : BaseUseCase() {

    @Throws(ErrorException::class)
    suspend operator fun invoke(): List<Report> {
        val reportsResponseModel = catchHttpExceptions(
            body = { reportService.getReports() },
            handler = { it.toErrorException() },
        )
        return reportsResponseModel.toReports(dateFormatter)
    }

    override fun HttpException.toErrorException(): ErrorException {
        return when (code()) {
            401 -> UnauthorizedErrorException(cause = this)
            else -> genericErrorException(this)
        }
    }
}
