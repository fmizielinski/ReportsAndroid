package pl.fmizielinski.reports.domain.usecase.report

import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.utils.createMultipartBody
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.ErrorReasons
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.error.errorException
import pl.fmizielinski.reports.domain.mapper.parseErrorBody
import pl.fmizielinski.reports.domain.usecase.base.BaseUseCase
import retrofit2.HttpException
import java.io.File

abstract class BaseAddAttachmentUseCase : BaseUseCase() {

    protected fun createFilePart(file: File) = file.createMultipartBody()

    protected fun HttpException.toErrorException(): ErrorException {
        return if (code() == 400) {
            val exceptions = parseErrorBody().map { error ->
                when (error.code) {
                    ErrorReasons.Report.Create.UPLOAD_FAILED -> error.errorException(
                        uiMessage = R.string.createReportScreen_error_addAttachment,
                        exception = this,
                    )

                    else -> genericErrorException(this)
                }
            }
            exceptions.asErrorException()
        } else if (code() == 403) {
            val exceptions = parseErrorBody().map { error ->
                when (error.code) {
                    ErrorReasons.Report.ACCESS_DENIED -> error.errorException(
                        uiMessage = R.string.createReportScreen_error_addAttachment,
                        exception = this,
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
            uiMessage = R.string.createReportScreen_error_addAttachment,
            message = "Unknown add attachment error",
            cause = cause,
        )
    }
}