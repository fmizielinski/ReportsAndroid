package pl.fmizielinski.reports.ui.reports

import app.cash.turbine.testIn
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.usecase.report.GetReportsUseCase
import pl.fmizielinski.reports.fixtures.domain.report
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.withFirst

class ReportsViewModelTest : BaseViewModelTest<ReportsViewModel>() {

    private val getReportsUseCase: GetReportsUseCase = mockk()

    override fun createViewModel(dispatcher: TestDispatcher): ReportsViewModel = ReportsViewModel(
        dispatcher = dispatcher,
        getReportsUseCase = getReportsUseCase,
    )

    @Test
    fun `GIVEN list of reports WHEN start THEN show reports list`() = runTurbineTest {
        val expectedId = 1
        val expectedTitle = "title"
        val expectedDescription = "description"
        val expectedReportDate = "12 Jun"
        val report = report(
            id = expectedId,
            title = expectedTitle,
            description = expectedDescription,
            reportDate = expectedReportDate,
        )
        coEvery { getReportsUseCase() } returns listOf(report)

        val uiState = viewModel.uiState.testIn(context)
        uiState.skipItems(1)

        viewModel.onStart()
        uiState.skipItems(1)

        val result = uiState.awaitItem()
        expectThat(result.reports).hasSize(1)
            .withFirst {
                get { id } isEqualTo expectedId
                get { title } isEqualTo expectedTitle
                get { description } isEqualTo expectedDescription
                get { reportDate } isEqualTo expectedReportDate
            }

        uiState.cancel()
    }
}
