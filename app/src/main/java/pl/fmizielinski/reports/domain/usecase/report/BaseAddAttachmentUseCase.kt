package pl.fmizielinski.reports.domain.usecase.report

import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import okhttp3.MultipartBody
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.utils.ProgressListener
import pl.fmizielinski.reports.data.network.utils.createMultipartBody
import pl.fmizielinski.reports.domain.error.ErrorException
import pl.fmizielinski.reports.domain.error.ErrorReasons
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.error.errorException
import pl.fmizielinski.reports.domain.mapper.parseErrorBody
import pl.fmizielinski.reports.domain.model.AttachmentData
import pl.fmizielinski.reports.domain.usecase.base.BaseUseCase
import retrofit2.HttpException
import java.io.File

abstract class BaseAddAttachmentUseCase<Data : AttachmentData, Result> : BaseUseCase() {

    operator fun invoke(data: Data): Flow<Result> {
        return callbackFlow {
            var isWriting = false
            var lastProgress = 0f
            val progressListener: ProgressListener = { progress ->
                val calculatedProgress = if (isWriting && progress < 1f) {
                    @Suppress("MagicNumber")
                    progress / 2 + 0.5f
                } else {
                    progress / 2
                }
                if (calculatedProgress > lastProgress) {
                    lastProgress = calculatedProgress
                    isWriting = isWriting || progress == 1f
                    trySendBlocking(getProgressResult(calculatedProgress))
                }
            }
            val filePart = createFilePart(data.file, progressListener)
            val result = getCompleteResult(data, filePart)
            trySendBlocking(result)
            close()
        }
            .distinctUntilChanged()
//            .conflate()
    }

    override fun genericErrorException(cause: Throwable): SimpleErrorException {
        return SimpleErrorException(
            uiMessage = R.string.createReportScreen_error_addAttachment,
            message = "Unknown add attachment error",
            cause = cause,
        )
    }

    protected fun createFilePart(
        file: File,
        progressListener: ProgressListener,
    ) = file.createMultipartBody(progressListener = progressListener)

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

    protected abstract fun getProgressResult(progress: Float): Result

    protected abstract suspend fun getCompleteResult(
        data: Data,
        filePart: MultipartBody.Part,
    ): Result
}
