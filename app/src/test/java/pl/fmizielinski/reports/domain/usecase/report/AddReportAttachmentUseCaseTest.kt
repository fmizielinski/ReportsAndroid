package pl.fmizielinski.reports.domain.usecase.report

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.error.ErrorReasons.Report.ACCESS_DENIED
import pl.fmizielinski.reports.domain.error.ErrorReasons.Report.Create.UPLOAD_FAILED
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.report.model.AttachmentUploadResult.Complete
import pl.fmizielinski.reports.domain.report.usecase.AddReportAttachmentUseCase
import pl.fmizielinski.reports.fixtures.common.httpException
import pl.fmizielinski.reports.fixtures.domain.addReportAttachmentData
import pl.fmizielinski.reports.fixtures.domain.networkError
import pl.fmizielinski.reports.utils.expectThrowable
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import java.io.File

class AddReportAttachmentUseCaseTest {

    private val reportService: ReportService = mockk()

    private val useCase = AddReportAttachmentUseCase(reportService)

    @Test
    fun `GIVEN valid attachment WHEN invoke THEN no errors`() = runTest {
        val reportId = 1
        val file = File.createTempFile("test", "jpg")
        val data = addReportAttachmentData(reportId, file)
        coJustRun { reportService.addAttachment(reportId, any()) }

        useCase(data).test {
            expectThat(awaitItem()).isA<Complete>()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN 400 UPLOAD_FAILED error WHEN invoke THEN throw upload failed exception`() = runTest {
        val errorMessage = "UPLOAD_FAILED"
        val exception = httpException<Unit>(
            code = 400,
            error = networkError(UPLOAD_FAILED, errorMessage),
        )
        val reportId = 1
        val file = File.createTempFile("test", "jpg")
        val data = addReportAttachmentData(reportId, file)
        coEvery { reportService.addAttachment(reportId, any()) } throws exception

        useCase(data).test {
            expectThrowable<SimpleErrorException>(awaitError()) {
                get { uiMessage } isEqualTo R.string.createReportScreen_error_addAttachment
                get { message } isEqualTo errorMessage
                get { isVerificationError }.isFalse()
            }
        }
    }

    @Test
    fun `GIVEN unknown 400 error WHEN invoke THEN throw generic exception`() = runTest {
        val exception = httpException<Unit>(
            code = 400,
            error = networkError("unknown", "message"),
        )
        val reportId = 1
        val file = File.createTempFile("test", "jpg")
        val data = addReportAttachmentData(reportId, file)
        coEvery { reportService.addAttachment(reportId, any()) } throws exception

        useCase(data).test {
            expectThrowable<SimpleErrorException>(awaitError()) {
                get { uiMessage } isEqualTo R.string.createReportScreen_error_addAttachment
                get { message } isEqualTo "Unknown add attachment error"
                get { isVerificationError }.isFalse()
            }
        }
    }

    @Test
    fun `GIVEN 403 ACCESS_DENIED error WHEN invoke THEN throw access denied exception`() = runTest {
        val errorMessage = "ACCESS_DENIED"
        val exception = httpException<Unit>(
            code = 403,
            error = networkError(ACCESS_DENIED, errorMessage),
        )
        val reportId = 1
        val file = File.createTempFile("test", "jpg")
        val data = addReportAttachmentData(reportId, file)
        coEvery { reportService.addAttachment(reportId, any()) } throws exception

        useCase(data).test {
            expectThrowable<SimpleErrorException>(awaitError()) {
                get { uiMessage } isEqualTo R.string.createReportScreen_error_addAttachment
                get { message } isEqualTo errorMessage
                get { isVerificationError }.isFalse()
            }
        }
    }

    @Test
    fun `GIVEN unknown 403 error WHEN invoke THEN throw generic exception`() = runTest {
        val exception = httpException<Unit>(
            code = 403,
            error = networkError("unknown", "message"),
        )
        val reportId = 1
        val file = File.createTempFile("test", "jpg")
        val data = addReportAttachmentData(reportId, file)
        coEvery { reportService.addAttachment(reportId, any()) } throws exception

        useCase(data).test {
            expectThrowable<SimpleErrorException>(awaitError()) {
                get { uiMessage } isEqualTo R.string.createReportScreen_error_addAttachment
                get { message } isEqualTo "Unknown add attachment error"
                get { isVerificationError }.isFalse()
            }
        }
    }

    @Test
    fun `GIVEN unknown http error WHEN invoke THEN throw generic exception`() = runTest {
        val reportId = 1
        val file = File.createTempFile("test", "jpg")
        val data = addReportAttachmentData(reportId, file)
        coEvery { reportService.addAttachment(reportId, any()) } throws httpException<Unit>(999)

        useCase(data).test {
            expectThrowable<SimpleErrorException>(awaitError()) {
                get { uiMessage } isEqualTo R.string.createReportScreen_error_addAttachment
                get { message } isEqualTo "Unknown add attachment error"
                get { isVerificationError }.isFalse()
            }
        }
    }
}
