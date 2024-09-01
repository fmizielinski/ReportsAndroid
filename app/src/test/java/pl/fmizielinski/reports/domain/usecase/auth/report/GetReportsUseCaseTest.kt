package pl.fmizielinski.reports.domain.usecase.auth.report

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.mapper.DataFormatter
import pl.fmizielinski.reports.domain.usecase.report.GetReportsUseCase
import pl.fmizielinski.reports.fixtures.data.reportModel
import pl.fmizielinski.reports.fixtures.data.reportsResponseModel
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.withFirst
import java.time.LocalDateTime

class GetReportsUseCaseTest {

    private val reportService: ReportService = mockk()
    private val dateFormatter: DataFormatter = mockk()

    private val useCase = GetReportsUseCase(reportService, dateFormatter)

    @Test
    fun `WHEN invoke THEN return reports`() = runTest {
        val expectedId = 1
        val expectedTitle = "title"
        val expectedDescription = "description"
        val reportDate = LocalDateTime.now()
        val formattedDate = "12 Jun"
        val reportModel = reportModel(
            id = expectedId,
            title = expectedTitle,
            description = expectedDescription,
            reportDate = reportDate,
        )
        val reportsResponseModel = reportsResponseModel(
            reports = listOf(reportModel),
        )
        coEvery { reportService.getReports() } returns reportsResponseModel
        every { dateFormatter.formatReportListDate(reportDate) } returns formattedDate

        val result = useCase()

        expectThat(result).hasSize(1)
            .withFirst {
                get { id } isEqualTo expectedId
                get { title } isEqualTo expectedTitle
                get { description } isEqualTo expectedDescription
                get { this.reportDate } isEqualTo formattedDate
            }
    }
}
