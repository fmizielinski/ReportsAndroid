package pl.fmizielinski.reports.ui.main.reports

import androidx.paging.PagingData
import app.cash.turbine.testIn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.report.usecase.GetReportsUseCase
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.repository.EventsRepository.GlobalEvent
import pl.fmizielinski.reports.fixtures.domain.report
import pl.fmizielinski.reports.ui.destinations.destinations.ReportDetailsDestination
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.UiEvent
import pl.fmizielinski.reports.ui.navigation.DestinationData

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
        coEvery { getReportsUseCase.data } returns flowOf(
            PagingData.from(
                listOf(report()),
            ),
        )

        val pagingContent = viewModel.pagingContent.testIn(context)

        scheduler.advanceUntilIdle()

        pagingContent.skipItems(1)
        pagingContent.awaitComplete()
        pagingContent.ensureAllEventsConsumed()

        pagingContent.cancel()
    }

    @Test
    fun `WHEN refresh THEN refresh reports`() = runTurbineTest {
        val reportsFlow = MutableStateFlow(
            PagingData.from(listOf(report()))
        )
        coEvery { getReportsUseCase.data } returns reportsFlow
        coEvery { getReportsUseCase() } coAnswers  {
            val data = PagingData.from(listOf(report(), report()))
            reportsFlow.emit(data)
        }

        val uiState = viewModel.uiState.testIn(context)
        val pagingContent = viewModel.pagingContent.testIn(context)

        scheduler.advanceUntilIdle()
        postUiEvent(UiEvent.Refresh)
        scheduler.advanceUntilIdle()

        pagingContent.skipItems(2)
        pagingContent.ensureAllEventsConsumed()

        pagingContent.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
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

    @Test
    fun `WHEN report clicked THEN post ReportDetailsDestination nav event`() = runTurbineTest {
        val id = 1
        val uiState = viewModel.uiState.testIn(context)

        postUiEvent(UiEvent.ReportClicked(id))
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postNavEvent(DestinationData(ReportDetailsDestination(id))) }

        uiState.cancelAndIgnoreRemainingEvents()
    }
}
