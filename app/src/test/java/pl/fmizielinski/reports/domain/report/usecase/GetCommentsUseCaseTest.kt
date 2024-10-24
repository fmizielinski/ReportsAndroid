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
import pl.fmizielinski.reports.fixtures.data.commentsResponseModel
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.withFirst

class GetCommentsUseCaseTest {

    private val reportService: ReportService = mockk()
    private val dateFormatter: DateFormatter = mockk()

    private val useCase = GetCommentsUseCase(reportService, dateFormatter)

    @Test
    fun `WHEN invoke THEN return comments`() = runTest {
        val reportId = 1
        val commentId = 2
        val comment = "comment"
        val user = "user"
        val createDate = "12 Jun"
        val isMine = true

        coEvery { reportService.getComments(reportId) } returns commentsResponseModel(
            commentModel(
                id = commentId,
                comment = comment,
                user = user,
                isMine = isMine,
            )
        )
        coEvery { dateFormatter.formatCommentDate(any(), any(), any(), any()) } returns createDate

        val result = useCase(reportId)
        expectThat(result).hasSize(1)
            .withFirst {
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

        coEvery {
            reportService.getComments(reportId)
        } throws httpException<ReportsResponseModel>(999)

        expectThrows<SimpleErrorException> {
            useCase(reportId)
        }.and {
            get { uiMessage } isEqualTo R.string.reportDetailsScreen_error_commentsLoading
            get { message } isEqualTo "Comments loading error"
        }
    }
}