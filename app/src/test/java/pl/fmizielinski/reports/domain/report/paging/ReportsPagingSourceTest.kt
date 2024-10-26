package pl.fmizielinski.reports.domain.report.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.report.model.Report
import pl.fmizielinski.reports.fixtures.data.reportModel
import pl.fmizielinski.reports.fixtures.data.reportsResponseModel
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.time.LocalDateTime

class ReportsPagingSourceTest {

    private val reportService: ReportService = mockk()
    private val dateFormatter: DateFormatter = mockk()

    private val source = ReportsPagingSource(reportService, dateFormatter)

    @Test
    fun `WHEN loadData THEN return reports`() = runTest {
        val expectedId = 1
        val expectedTitle = "title"
        val expectedDescription = "description"
        val reportDate = LocalDateTime.now()
        val formattedDate = "12 Jun"
        val expectedComments = 2
        val reportModel = reportModel(
            id = expectedId,
            title = expectedTitle,
            description = expectedDescription,
            reportDate = reportDate,
            comments = expectedComments,
        )
        val reportsResponseModel = reportsResponseModel(
            reports = listOf(reportModel),
        )
        coEvery { reportService.getReports(any(), any()) } returns reportsResponseModel
        every { dateFormatter.formatReportListDate(reportDate, any()) } returns formattedDate

        val pager = TestPager(PagingConfig(1), source)
        val result = pager.refresh()

        expectThat(result).isA<PagingSource.LoadResult.Page<Int, Report>>()
            .and {
                get { data }.all {
                    get { id } isEqualTo expectedId
                    get { title } isEqualTo expectedTitle
                    get { description } isEqualTo expectedDescription
                    get { this.reportDate } isEqualTo formattedDate
                    get { this.comments } isEqualTo expectedComments
                }
            }
    }
}
