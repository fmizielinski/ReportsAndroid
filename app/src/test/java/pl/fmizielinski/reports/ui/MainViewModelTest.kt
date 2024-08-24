package pl.fmizielinski.reports.ui

import app.cash.turbine.testIn
import com.ramcosta.composedestinations.generated.destinations.LoginDestination
import com.ramcosta.composedestinations.generated.destinations.RegisterDestination
import com.ramcosta.composedestinations.generated.navgraphs.AuthNavGraph
import com.ramcosta.composedestinations.generated.navgraphs.MainNavGraph
import com.ramcosta.composedestinations.utils.startDestination
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.auth.IsLoggedInUseCase
import pl.fmizielinski.reports.ui.MainViewModel.UiEvent
import pl.fmizielinski.reports.ui.model.TopBarAction
import pl.fmizielinski.reports.ui.navigation.toDestinationData
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

@ExperimentalCoroutinesApi
class MainViewModelTest : BaseViewModelTest<MainViewModel>() {
    private val eventsRepository: EventsRepository = EventsRepository()
    private val isLoggedInUseCase: IsLoggedInUseCase = mockk()

    override fun createViewModel(dispatcher: TestDispatcher): MainViewModel = MainViewModel(
        dispatcher = dispatcher,
        eventsRepository = eventsRepository,
        isLoggedInUseCase = isLoggedInUseCase,
    )

    @Test
    fun `WHEN Snackbar event is posted THEN Show Snackbar`() = runTurbineTest {
        val snackBarData = SnackBarData(0, SnackBarData.DURATION_SHORT)

        val showSnackBar = viewModel.showSnackBar.testIn(context, name = "showSnackBar")

        context.launch { eventsRepository.postSnackBarEvent(snackBarData) }
        scheduler.advanceUntilIdle()

        expectThat(showSnackBar.awaitItem()) isEqualTo snackBarData
        expectThat(showSnackBar.awaitItem()) isEqualTo SnackBarData.empty()

        showSnackBar.cancel()
    }

    @Test
    fun `WHEN Navigation event is posted THEN Navigate to destination`() = runTurbineTest {
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")
        val destinationData = LoginDestination.toDestinationData()

        context.launch { eventsRepository.postNavEvent(destinationData) }
        scheduler.advanceUntilIdle()

        expectThat(navigationEvents.awaitItem().get()) isEqualTo destinationData

        navigationEvents.cancel()
    }

    @Test
    fun `WHEN Navigation up event is posted THEN Navigate up`() = runTurbineTest {
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { eventsRepository.postNavUpEvent() }
        scheduler.advanceUntilIdle()

        expectThat(navigationEvents.awaitItem().isPresent).isFalse()

        navigationEvents.cancel()
    }

    @Test
    fun `WHEN Current destination is 'LoginScreen' THEN hide back button`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns false

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        uiState.skipItems(1)
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.NavDestinationChanged(LoginDestination.baseRoute)) }
        uiState.skipItems(1)

        val result = uiState.awaitItem()
        expectThat(result.isBackVisible).isFalse()

        uiState.cancel()
        navigationEvents.cancel()
    }

    @Test
    fun `WHEN Current destination is 'LoginScreen' THEN show register action`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns false

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        uiState.skipItems(1)
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.NavDestinationChanged(LoginDestination.baseRoute)) }
        uiState.skipItems(1)

        val result = uiState.awaitItem()
        expectThat(result.actions).isEqualTo(listOf(TopBarAction.REGISTER))

        uiState.cancel()
        navigationEvents.cancel()
    }

    @Test
    fun `WHEN Current destination is 'RegisterScreen' THEN show back button`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns false

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        uiState.skipItems(1)
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.NavDestinationChanged(RegisterDestination.baseRoute)) }
        uiState.skipItems(1)

        val result = uiState.awaitItem()
        expectThat(result.isBackVisible).isTrue()

        uiState.cancel()
        navigationEvents.cancel()
    }

    @Test
    fun `WHEN Back button is clicked THEN Navigate up`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.BackClicked) }

        expectThat(navigationEvents.awaitItem().isPresent).isFalse()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN Register button is clicked THEN Navigate to RegisterScreen`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.RegisterClicked) }

        expectThat(navigationEvents.awaitItem().get()) isEqualTo RegisterDestination.toDestinationData()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN user is logged in WHEN app starts THEN navigate to MainNavGraph start destination`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns true

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.NavDestinationChanged(AuthNavGraph.startDestination.route)) }

        expectThat(navigationEvents.awaitItem().get()) isEqualTo MainNavGraph.startDestination.toDestinationData()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN user is not logged in WHEN app starts THEN navigate to AuthNavGraph start destination`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns false

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.NavDestinationChanged(AuthNavGraph.startDestination.route)) }

        navigationEvents.expectNoEvents()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }
}
