package pl.fmizielinski.reports.ui.reports

import app.cash.turbine.testIn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.common.model.SnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.repository.EventsRepository.GlobalEvent
import pl.fmizielinski.reports.domain.report.usecase.GetReportsUseCase
import pl.fmizielinski.reports.fixtures.domain.report
import pl.fmizielinski.reports.fixtures.domain.simpleErrorException
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.UiEvent
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import strikt.assertions.withFirst

class ReportsViewModelTest : BaseViewModelTest<ReportsViewModel, UiEvent>() {

    private val getReportsUseCase: GetReportsUseCase = mockk()
    private val eventsRepository = spyk(EventsRepository())

    override fun createViewModel(dispatcher: TestDispatcher): ReportsViewModel = ReportsViewModel(
        dispatcher = dispatcher,
        getReportsUseCase = getReportsUseCase,
        eventsRepository = eventsRepository,
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

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()

        val result = uiState.expectMostRecentItem()
        expectThat(result.reports).hasSize(1)
            .withFirst {
                get { id } isEqualTo expectedId
                get { title } isEqualTo expectedTitle
                get { description } isEqualTo expectedDescription
                get { reportDate } isEqualTo expectedReportDate
            }

        uiState.cancel()
    }

    @Test
    fun `GIVEN reports loading error WHEN start THEN show snackbar`() = runTurbineTest {
        val errorException = simpleErrorException()
        val snackBarData = SnackBarData(messageResId = R.string.common_error_oops)
        coEvery { getReportsUseCase() } throws errorException

        val uiState = viewModel.uiState.testIn(context)

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(snackBarData) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN refresh THEN refresh reports`() = runTurbineTest {
        val report = report()
        coEvery { getReportsUseCase() } returnsMany listOf(
            listOf(report),
            listOf(report, report),
        )

        val uiState = viewModel.uiState.testIn(context)

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()
        postUiEvent(UiEvent.Refresh)
        uiState.skipItems(4)

        expectThat(uiState.awaitItem()) {
            get { isLoading }.isTrue()
            get { isRefreshing }.isTrue()
        }

        scheduler.advanceUntilIdle()
        expectThat(uiState.expectMostRecentItem()) {
            get { isLoading }.isFalse()
            get { isRefreshing }.isFalse()
            get { reports }.hasSize(2)
        }
        coVerify(exactly = 2) { getReportsUseCase() }

        uiState.cancel()
    }

    @Test
    fun `WHEN list scrolled with first index 0 THEN post ChangeFabVisibility true event`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context)

        postUiEvent(UiEvent.ListScrolled(0))
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postGlobalEvent(GlobalEvent.ChangeFabVisibility(true)) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN list scrolled with first index not 0 THEN post ChangeFabVisibility false event`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context)

        postUiEvent(UiEvent.ListScrolled(10))
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postGlobalEvent(GlobalEvent.ChangeFabVisibility(false)) }

        uiState.cancelAndIgnoreRemainingEvents()
    }
}
