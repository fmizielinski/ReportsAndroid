package pl.fmizielinski.reports.domain.report.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.data.network.report.model.ReportDetailsResponseModel
import pl.fmizielinski.reports.domain.error.SimpleErrorException
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.utils.PathProvider
import pl.fmizielinski.reports.fixtures.common.httpException
import pl.fmizielinski.reports.fixtures.data.attachmentModel
import pl.fmizielinski.reports.fixtures.data.reportDetailsResponseModel
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.hasSize
import pl.fmizielinski.reports.R
import strikt.assertions.isEqualTo
import strikt.assertions.withFirst
import java.time.LocalDateTime

class GetReportDetailsUseCaseTest {

    private val reportService: ReportService = mockk()
    private val dateFormatter: DateFormatter = mockk()
    private val pathProvider: PathProvider = mockk()

    private val useCase = GetReportDetailsUseCase(reportService, dateFormatter, pathProvider)

    @Test
    fun `WHEN invoke THEN return report details`() = runTest {
        val expectedId = 1
        val expectedTitle = "title"
        val expectedDescription = "description"
        val reportDate = LocalDateTime.now()
        val formattedDate = "12 Jun 2021, 12:00"
        val expectedAttachmentId = 3
        val attachmentPath = "attachmentPath"
        val attachments = listOf(
            attachmentModel(id = expectedAttachmentId),
        )
        val reportDetailsResponseModel = reportDetailsResponseModel(
            id = expectedId,
            title = expectedTitle,
            description = expectedDescription,
            reportDate = reportDate,
            attachments = attachments,
        )
        coEvery { reportService.getReportDetails(expectedId) } returns reportDetailsResponseModel
        every { dateFormatter.formatReportDetailsDate(reportDate) } returns formattedDate
        every { pathProvider.getAttachmentPath() } returns attachmentPath

        val result = useCase(expectedId)

        expectThat(result) {
            get { id } isEqualTo expectedId
            get { title } isEqualTo expectedTitle
            get { description } isEqualTo expectedDescription
            get { this.reportDate } isEqualTo formattedDate
            get { this.attachments }.hasSize(1)
                .withFirst {
                    get { id } isEqualTo expectedAttachmentId
                    get { path } isEqualTo "$attachmentPath$expectedAttachmentId"
                }
        }
    }

    @Test
    fun `GIVEN http error WHEN invoke THEN throw generic error`() = runTest {
        val id = 1
        coEvery { reportService.getReportDetails(id) } throws httpException<ReportDetailsResponseModel>(404)

        expectThrows<SimpleErrorException> {
            useCase(id)
        } and {
            get { uiMessage } isEqualTo R.string.reportDetailsScreen_error_loading
            get { message } isEqualTo "Report details loading error"
        }
    }
}
