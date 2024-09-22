package pl.fmizielinski.reports.domain.usecase.report

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.error.ErrorReasons.Report.ACCESS_DENIED
import pl.fmizielinski.reports.domain.error.ErrorReasons.Report.Create.UPLOAD_FAILED
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.fixtures.common.httpException
import pl.fmizielinski.reports.fixtures.data.addTemporaryAttachmentResponse
import pl.fmizielinski.reports.fixtures.domain.networkError
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import java.io.File

class AddTemporaryAttachmentUseCaseTest {

    private val reportService: ReportService = mockk()

    private val useCase = AddTemporaryAttachmentUseCase(reportService)

    @Test
    fun `GIVEN valid attachment WHEN invoke THEN return uuid`() = runTest {
        val uuid = "uuid"
        val file = File.createTempFile("test", "jpg")
        coEvery { reportService.addTemporaryAttachment(any()) } returns addTemporaryAttachmentResponse(uuid)

        val result = useCase(file)
        expectThat(result) isEqualTo uuid
    }

    @Test
    fun `GIVEN 400 UPLOAD_FAILED error WHEN invoke THEN throw upload failed exception`() = runTest {
        val errorMessage = "UPLOAD_FAILED"
        val exception = httpException<Unit>(
            code = 400,
            error = networkError(UPLOAD_FAILED, errorMessage),
        )
        val file = File.createTempFile("test", "jpg")

        coEvery { reportService.addTemporaryAttachment(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(file)
        }.and {
            get { uiMessage } isEqualTo R.string.createReportScreen_error_addAttachment
            get { message } isEqualTo errorMessage
            get { isVerificationError }.isFalse()
        }
    }

    @Test
    fun `GIVEN unknown 400 error WHEN invoke THEN throw generic exception`() = runTest {
        val exception = httpException<Unit>(
            code = 400,
            error = networkError("unknown", "message"),
        )
        val file = File.createTempFile("test", "jpg")

        coEvery { reportService.addTemporaryAttachment(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(file)
        }.and {
            get { uiMessage } isEqualTo R.string.createReportScreen_error_addAttachment
            get { message } isEqualTo "Unknown add attachment error"
            get { isVerificationError }.isFalse()
        }
    }

    @Test
    fun `GIVEN 403 ACCESS_DENIED error WHEN invoke THEN throw access denied exception`() = runTest {
        val errorMessage = "ACCESS_DENIED"
        val exception = httpException<Unit>(
            code = 403,
            error = networkError(ACCESS_DENIED, errorMessage),
        )
        val file = File.createTempFile("test", "jpg")

        coEvery { reportService.addTemporaryAttachment(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(file)
        }.and {
            get { uiMessage } isEqualTo R.string.createReportScreen_error_addAttachment
            get { message } isEqualTo errorMessage
            get { isVerificationError }.isFalse()
        }
    }

    @Test
    fun `GIVEN unknown 403 error WHEN invoke THEN throw generic exception`() = runTest {
        val exception = httpException<Unit>(
            code = 403,
            error = networkError("unknown", "message"),
        )
        val file = File.createTempFile("test", "jpg")

        coEvery { reportService.addTemporaryAttachment(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(file)
        }.and {
            get { uiMessage } isEqualTo R.string.createReportScreen_error_addAttachment
            get { message } isEqualTo "Unknown add attachment error"
            get { isVerificationError }.isFalse()
        }
    }

    @Test
    fun `GIVEN unknown http error WHEN invoke THEN throw generic exception`() = runTest {
        val file = File.createTempFile("test", "jpg")

        coEvery { reportService.addTemporaryAttachment(any()) } throws httpException<Unit>(999)

        expectThrows<SimpleErrorException> {
            useCase(file)
        }.and {
            get { uiMessage } isEqualTo R.string.createReportScreen_error_addAttachment
            get { message } isEqualTo "Unknown add attachment error"
            get { isVerificationError }.isFalse()
        }
    }
}
