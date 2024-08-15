package pl.fmizielinski.reports.ui

import app.cash.turbine.testIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import org.junit.Test
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExperimentalCoroutinesApi
class MainViewModelTest : BaseViewModelTest() {
    private val eventsRepository: EventsRepository = EventsRepository()

    private fun viewModel(scheduler: TestCoroutineScheduler) = MainViewModel(
        dispatcher = StandardTestDispatcher(scheduler),
        eventsRepository = eventsRepository,
    )

    @Test
    fun `WHEN Snackbar event is posted THEN Show Snackbar`() = runTurbineTest {
        val snackBarData = SnackBarData(0, SnackBarData.DURATION_SHORT)

        val viewModel = viewModel(scheduler)
        val showSnackBar = viewModel.showSnackBar.testIn(context, name = "showSnackBar")

        context.launch { eventsRepository.postSnackBarEvent(snackBarData) }
        scheduler.advanceUntilIdle()

        expectThat(showSnackBar.awaitItem()) isEqualTo snackBarData
        expectThat(showSnackBar.awaitItem()) isEqualTo SnackBarData.empty()

        showSnackBar.cancel()
    }
}
