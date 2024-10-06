package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.ErrorReasons
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.error.errorException
import pl.fmizielinski.reports.domain.mapper.parseErrorBody
import pl.fmizielinski.reports.domain.report.model.CreateReportData
import pl.fmizielinski.reports.domain.report.model.toCreateReportRequest
import pl.fmizielinski.reports.domain.base.BaseUseCase
import retrofit2.HttpException

@Factory
class CreateReportUseCase(
    private val reportService: ReportService,
) : BaseUseCase() {

    @Throws(ErrorException::class)
    suspend operator fun invoke(data: CreateReportData) {
        val requestModel = data.toCreateReportRequest()
        catchHttpExceptions(
            body = { reportService.createReport(requestModel) },
            handler = { it.toErrorException() },
        )
    }

    override fun HttpException.toErrorException(): ErrorException {
        return if (code() == 400) {
            val exceptions = parseErrorBody().map { error ->
                when (error.code) {
                    ErrorReasons.Report.Create.INVALID_DATA -> error.errorException(
                        uiMessage = R.string.createReportScreen_error_save,
                        exception = this,
                    )

                    ErrorReasons.Report.Create.TITLE_EMPTY -> error.errorException(
                        uiMessage = R.string.createReportScreen_error_titleEmpty,
                        exception = this,
                        isVerificationError = true,
                    )

                    ErrorReasons.Report.Create.DESCRIPTION_EMPTY -> error.errorException(
                        uiMessage = R.string.createReportScreen_error_descriptionEmpty,
                        exception = this,
                        isVerificationError = true,
                    )

                    else -> genericErrorException(this)
                }
            }
            exceptions.asErrorException()
        } else {
            genericErrorException(this)
        }
    }

    override fun genericErrorException(cause: Throwable): SimpleErrorException {
        return SimpleErrorException(
            uiMessage = R.string.createReportScreen_error_save,
            message = "Unknown create report error",
            cause = cause,
        )
    }
}
