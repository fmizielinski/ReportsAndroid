package pl.fmizielinski.reports.domain.usecase.report

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.UnauthorizedException
import pl.fmizielinski.reports.domain.mapper.DataFormatter
import pl.fmizielinski.reports.domain.mapper.toReports
import pl.fmizielinski.reports.domain.model.Report
import pl.fmizielinski.reports.domain.usecase.BaseUseCase
import retrofit2.HttpException

@Factory
class GetReportsUseCase(
    private val reportService: ReportService,
    private val dateFormatter: DataFormatter,
) : BaseUseCase() {

    @Throws(ErrorException::class)
    suspend operator fun invoke(): List<Report> {
        val reportsResponseModel = catchHttpExceptions(
            body = { reportService.getReports() },
            handler = { it.toErrorException() },
        )
        return reportsResponseModel.toReports(dateFormatter)
    }

    private fun HttpException.toErrorException(): ErrorException {
        return when (code()) {
            401 -> UnauthorizedException(cause = this)
            else -> genericErrorException(this)
        }
    }
}
