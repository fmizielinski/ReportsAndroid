package pl.fmizielinski.reports.domain.usecase.report

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.error.ErrorReasons
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.fixtures.common.httpException
import pl.fmizielinski.reports.fixtures.domain.createReportData
import pl.fmizielinski.reports.fixtures.domain.networkError
import strikt.api.expectDoesNotThrow
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class CreateReportUseCaseTest {

    private val reportService: ReportService = mockk()

    private val useCase = CreateReportUseCase(reportService)

    @Test
    fun `GIVEN valid report data WHEN invoke THEN no errors`() = runTest {
        coJustRun { reportService.createReport(any()) }

        expectDoesNotThrow { useCase(createReportData()) }
    }

    @Test
    fun `GIVEN 400 INVALID_DATA error WHEN invoke THEN throw invalid data exception`() = runTest {
        val message = "INVALID_DATA"
        val exception = httpException<Unit>(
            code = 400,
            error = networkError(ErrorReasons.Report.Create.INVALID_DATA, message),
        )
        coEvery { reportService.createReport(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(createReportData())
        }.and {
            get { uiMessage } isEqualTo R.string.createReportScreen_error_save
            get { message } isEqualTo message
            get { isVerificationError }.isFalse()
        }
    }

    @Test
    fun `GIVEN 400 TITLE_EMPTY error WHEN invoke THEN throw empty title exception`() = runTest {
        val message = "TITLE_EMPTY"
        val exception = httpException<Unit>(
            code = 400,
            error = networkError(ErrorReasons.Report.Create.TITLE_EMPTY, message),
        )
        coEvery { reportService.createReport(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(createReportData())
        }.and {
            get { uiMessage } isEqualTo R.string.createReportScreen_error_titleEmpty
            get { message } isEqualTo message
            get { isVerificationError }.isTrue()
        }
    }

    @Test
    fun `GIVEN 400 DESCRIPTION_EMPTY error WHEN invoke THEN throw empty description exception`() = runTest {
        val message = "DESCRIPTION_EMPTY"
        val exception = httpException<Unit>(
            code = 400,
            error = networkError(ErrorReasons.Report.Create.DESCRIPTION_EMPTY, message),
        )
        coEvery { reportService.createReport(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(createReportData())
        }.and {
            get { uiMessage } isEqualTo R.string.createReportScreen_error_descriptionEmpty
            get { message } isEqualTo message
            get { isVerificationError }.isTrue()
        }
    }

    @Test
    fun `GIVEN unknown http error WHEN invoke THEN throw unknown error exception`() = runTest {
        coEvery { reportService.createReport(any()) } throws httpException<Unit>(999)

        expectThrows<SimpleErrorException> {
            useCase(createReportData())
        }.and {
            get { uiMessage } isEqualTo R.string.createReportScreen_error_save
            get { message } isEqualTo "Unknown create report error"
            get { isVerificationError }.isFalse()
        }
    }

    @Test
    fun `GIVEN unknown 400 error WHEN invoke THEN throw unknown error exception`() = runTest {
        val exception = httpException<Unit>(
            code = 400,
            error = networkError("unknown", "message"),
        )
        coEvery { reportService.createReport(any()) } throws exception

        expectThrows<SimpleErrorException> {
            useCase(createReportData())
        }.and {
            get { uiMessage } isEqualTo R.string.createReportScreen_error_save
            get { message } isEqualTo "Unknown create report error"
            get { isVerificationError }.isFalse()
        }
    }
}
