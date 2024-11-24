package pl.fmizielinski.reports.domain.report.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.report.model.Comment
import pl.fmizielinski.reports.fixtures.data.commentModel
import pl.fmizielinski.reports.fixtures.data.commentsResponseModel
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class CommentsPagingSourceTest {

    private val reportId = 1
    private val reportService: ReportService = mockk()
    private val dateFormatter: DateFormatter = mockk()

    private val source = CommentsPagingSource(reportId, reportService, dateFormatter)

    @Test
    fun `WHEN loadData THEN return reports`() = runTest {
        val commentId = 2
        val comment = "comment"
        val user = "user"
        val createDate = "12 Jun"
        val isMine = true
        val response = commentsResponseModel(
            commentModel(
                id = commentId,
                comment = comment,
                user = user,
                isMine = isMine,
            ),
        )
        coEvery { reportService.getComments(reportId, any(), any()) } returns response
        coEvery { dateFormatter.formatCommentDate(any(), any(), any(), any()) } returns createDate

        val pager = TestPager(PagingConfig(1), source)
        val result = pager.refresh()

        expectThat(result).isA<PagingSource.LoadResult.Page<Int, Comment>>()
            .and {
                get { data }.all {
                    get { id } isEqualTo commentId
                    get { this.comment } isEqualTo comment
                    get { this.user } isEqualTo user
                    get { this.createDate } isEqualTo createDate
                    get { this.isMine } isEqualTo isMine
                }
            }
    }
}
