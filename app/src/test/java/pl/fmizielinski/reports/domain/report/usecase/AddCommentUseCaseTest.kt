package pl.fmizielinski.reports.domain.report.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.data.network.report.model.ReportsResponseModel
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.fixtures.common.httpException
import pl.fmizielinski.reports.fixtures.data.commentModel
import pl.fmizielinski.reports.fixtures.domain.addCommentData
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class AddCommentUseCaseTest {

    private val reportService: ReportService = mockk()
    private val dateFormatter: DateFormatter = mockk()

    private val useCase = AddCommentUseCase(reportService, dateFormatter)

    @Test
    fun `GIVEN valid comment data WHEN invoke THEN return added comment`() = runTest {
        val reportId = 1
        val commentId = 2
        val comment = "comment"
        val user = "user"
        val createDate = "12 Jun"
        val isMine = true
        val commentData = addCommentData()

        coEvery { reportService.addComment(reportId, any()) } returns commentModel(
            id = commentId,
            comment = comment,
            user = user,
            isMine = isMine,
        )
        coEvery { dateFormatter.formatCommentDate(any(), any(), any(), any()) } returns createDate

        val result = useCase(reportId, commentData)
        expectThat(result) {
            get { id } isEqualTo commentId
            get { this.comment } isEqualTo comment
            get { this.user } isEqualTo user
            get { this.createDate } isEqualTo createDate
            get { this.isMine } isEqualTo isMine
        }
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
