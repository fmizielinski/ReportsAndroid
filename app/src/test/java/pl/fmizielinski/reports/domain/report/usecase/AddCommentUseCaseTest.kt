package pl.fmizielinski.reports.domain.report.usecase

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.data.network.report.model.ReportsResponseModel
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.fixtures.common.httpException
import pl.fmizielinski.reports.fixtures.domain.addCommentData
import strikt.api.expectDoesNotThrow
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class AddCommentUseCaseTest {

    private val reportService: ReportService = mockk()

    private val useCase = AddCommentUseCase(reportService)

    @Test
    fun `GIVEN valid comment data WHEN invoke THEN return added comment`() = runTest {
        val reportId = 1
        val commentData = addCommentData()

        coJustRun { reportService.addComment(reportId, any()) }

        expectDoesNotThrow { useCase(reportId, commentData) }
    }

    @Test
    fun `GIVEN Unknown error WHEN invoke THEN throw generic error exception`() = runTest {
        val reportId = 1
        val commentData = addCommentData()
        coEvery { reportService.addComment(any(), any()) } throws httpException<ReportsResponseModel>(999)

        expectThrows<SimpleErrorException> {
            useCase(reportId, commentData)
        }.and {
            get { uiMessage } isEqualTo R.string.common_error_unknown
            get { message } isEqualTo "Unknown error"
        }
    }
}
