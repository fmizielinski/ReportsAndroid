package pl.fmizielinski.reports.ui

import android.Manifest
import app.cash.turbine.testIn
import com.ramcosta.composedestinations.generated.destinations.CreateReportDestination
import com.ramcosta.composedestinations.generated.destinations.LoginDestination
import com.ramcosta.composedestinations.generated.destinations.RegisterDestination
import com.ramcosta.composedestinations.generated.destinations.ReportsDestination
import com.ramcosta.composedestinations.generated.navgraphs.AuthNavGraph
import com.ramcosta.composedestinations.generated.navgraphs.MainNavGraph
import com.ramcosta.composedestinations.utils.startDestination
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.auth.IsLoggedInUseCase
import pl.fmizielinski.reports.domain.usecase.auth.LogoutUseCase
import pl.fmizielinski.reports.fixtures.ui.alertDialogUiState
import pl.fmizielinski.reports.ui.MainViewModel.UiEvent
import pl.fmizielinski.reports.ui.MainViewModel.UiState
import pl.fmizielinski.reports.ui.common.model.TopBarAction
import pl.fmizielinski.reports.ui.navigation.toDestinationData
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import java.io.File

@ExperimentalCoroutinesApi
class MainViewModelTest : BaseViewModelTest<MainViewModel>() {
    private val eventsRepository: EventsRepository = spyk { EventsRepository() }
    private val isLoggedInUseCase: IsLoggedInUseCase = mockk()
    private val logoutUseCase: LogoutUseCase = mockk()

    override fun createViewModel(dispatcher: TestDispatcher): MainViewModel = MainViewModel(
        dispatcher = dispatcher,
        eventsRepository = eventsRepository,
        isLoggedInUseCase = isLoggedInUseCase,
        logoutUseCase = logoutUseCase,
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
    fun `WHEN Logout event is posted THEN navigate to AuthNavGraph start destination AND Show Snackbar`() = runTurbineTest {
        coJustRun { logoutUseCase() }

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { eventsRepository.postLogoutEvent() }
        scheduler.advanceUntilIdle()

        expectThat(navigationEvents.awaitItem().get()) isEqualTo AuthNavGraph.startDestination.toDestinationData()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @ParameterizedTest
    @MethodSource("destinationBackButton")
    fun `WHEN Destination changed  THEN change back button visibility`(
        destination: String,
        isBackVisible: Boolean,
    ) = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns false

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        uiState.skipItems(1)
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.NavDestinationChanged(destination)) }
        uiState.skipItems(1)

        val result = uiState.awaitItem()
        expectThat(result.appBarUiState.isBackVisible) isEqualTo isBackVisible

        uiState.cancel()
        navigationEvents.cancel()
    }

    @ParameterizedTest
    @MethodSource("destinationActions")
    fun `WHEN Destination changed THEN change actions`(
        destination: String,
        action: List<TopBarAction>,
    ) = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns false

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        uiState.skipItems(1)
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.NavDestinationChanged(destination)) }
        uiState.skipItems(1)

        val result = uiState.awaitItem()
        expectThat(result.appBarUiState.actions) isEqualTo action

        uiState.cancel()
        navigationEvents.cancel()
    }

    @ParameterizedTest
    @MethodSource("destinationFabConfig")
    fun `WHEN Destination changed THEN change fab configuration`(
        destination: String,
        fabConfig: UiState.FabConfig?,
    ) = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns false

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        uiState.skipItems(1)
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.NavDestinationChanged(destination)) }
        uiState.skipItems(1)

        val result = uiState.awaitItem()
        expectThat(result.fabConfig) isEqualTo fabConfig

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
    fun `WHEN REGISTER action is clicked THEN Navigate to RegisterScreen`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.ActionClicked(TopBarAction.REGISTER)) }

        expectThat(navigationEvents.awaitItem().get()) isEqualTo RegisterDestination.toDestinationData()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN PHOTO action is clicked THEN take picture`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val takePicture = viewModel.takePicture.testIn(context, name = "takePicture")

        context.launch { viewModel.postUiEvent(UiEvent.ActionClicked(TopBarAction.PHOTO)) }
        scheduler.advanceUntilIdle()

        takePicture.expectMostRecentItem()

        takePicture.cancel()
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
    fun `GIVEN user is not logged in WHEN app starts THEN Don't navigate further`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns false

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch { viewModel.postUiEvent(UiEvent.NavDestinationChanged(AuthNavGraph.startDestination.route)) }

        navigationEvents.expectNoEvents()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN Current destination is Reports WHEN fab clicked THEN Navigate to CreateReport`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns true

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch {
            viewModel.postUiEvent(UiEvent.NavDestinationChanged(LoginDestination.baseRoute))
            viewModel.postUiEvent(UiEvent.NavDestinationChanged(ReportsDestination.baseRoute))
            navigationEvents.skipItems(1)
            viewModel.postUiEvent(UiEvent.FabClicked)
            navigationEvents.skipItems(1)
        }
        scheduler.advanceUntilIdle()

        expectThat(navigationEvents.awaitItem().get()) isEqualTo CreateReportDestination.toDestinationData()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN Current destination is CreateReport WHEN fab clicked THEN Post save event AND hide fab`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns true

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        uiState.skipItems(1)
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch {
            viewModel.postUiEvent(UiEvent.NavDestinationChanged(LoginDestination.baseRoute))
            viewModel.postUiEvent(UiEvent.NavDestinationChanged(CreateReportDestination.baseRoute))
            viewModel.postUiEvent(UiEvent.FabClicked)
        }
        scheduler.advanceUntilIdle()
        uiState.skipItems(7)

        expectThat(uiState.awaitItem().fabConfig).isNull()
        coVerify(exactly = 1) { eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.SaveReport) }

        navigationEvents.cancelAndIgnoreRemainingEvents()
        uiState.cancel()
    }

    @Test
    fun `GIVEN Current destination is CreateReport AND fab is hidden WHEN SaveReportFailed event posted THEN show fab`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns true

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        uiState.skipItems(1)
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        context.launch {
            viewModel.postUiEvent(UiEvent.NavDestinationChanged(LoginDestination.baseRoute))
            viewModel.postUiEvent(UiEvent.NavDestinationChanged(CreateReportDestination.baseRoute))
            viewModel.postUiEvent(UiEvent.FabClicked)
            eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.SaveReportFailed)
        }
        scheduler.advanceUntilIdle()
        uiState.skipItems(8)

        expectThat(uiState.awaitItem().fabConfig) isEqualTo UiState.FabConfig(
            icon = R.drawable.ic_save_24dp,
            contentDescription = R.string.common_button_saveReport,
        )
        coVerify(exactly = 1) { eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.SaveReport) }

        navigationEvents.cancelAndIgnoreRemainingEvents()
        uiState.cancel()
    }

    @Test
    fun `WHEN PictureTaken event posted THEN post PictureTaken global event`() = runTurbineTest {
        val file = File.createTempFile("test", "jpg")

        val uiState = viewModel.uiState.testIn(context, name = "uiState")

        context.launch {
            viewModel.postUiEvent(UiEvent.PictureTaken(file))
        }
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.PictureTaken(file)) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN TakePictureFailed event posted THEN post snackbar event`() = runTurbineTest {
        val snackBarData = SnackBarData(messageResId = R.string.common_error_ups)

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val showSnackBar = viewModel.showSnackBar.testIn(context, name = "showSnackBar")

        context.launch {
            viewModel.postUiEvent(UiEvent.TakePictureFailed)
        }
        scheduler.advanceUntilIdle()

        expectThat(showSnackBar.awaitItem()) isEqualTo snackBarData
        expectThat(showSnackBar.awaitItem()) isEqualTo SnackBarData.empty()

        uiState.cancelAndIgnoreRemainingEvents()
        showSnackBar.cancel()
    }

    @Test
    fun `WHEN ShowPermissionRationale event posted THEN show alert dialog`() = runTurbineTest {
        val expectedIconResId = R.drawable.ic_info_24dp
        val expectedTitleResId = R.string.common_label_permission
        val expectedMessageResId = R.string.common_label_cameraPermissionRationale
        val expectedPositiveButtonResId = R.string.common_label_settings
        val expectedNegativeButtonResId = R.string.common_label_cancel

        val expected = alertDialogUiState(
            iconResId = expectedIconResId,
            titleResId = expectedTitleResId,
            messageResId = expectedMessageResId,
            positiveButtonResId = expectedPositiveButtonResId,
            negativeButtonResId = expectedNegativeButtonResId,
        )

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        uiState.skipItems(1)

        context.launch {
            viewModel.postUiEvent(UiEvent.ShowPermissionRationale(Manifest.permission.CAMERA))
        }

        val result = uiState.awaitItem()
        expectThat(result.alertDialogUiState) isEqualTo expected

        uiState.cancel()
    }

    @Test
    fun `GIVEN alert dialog visible WHEN AlertDialogDismissed event posted THEN hide alert dialog`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        uiState.skipItems(1)

        context.launch {
            viewModel.postUiEvent(UiEvent.ShowPermissionRationale(Manifest.permission.CAMERA))
            viewModel.postUiEvent(UiEvent.AlertDialogDismissed)
        }
        uiState.skipItems(1)

        val result = uiState.awaitItem()
        expectThat(result.alertDialogUiState).isNull()

        uiState.cancel()
    }

    @Test
    fun `GIVEN alert dialog visible WHEN AlertDialogPositiveClicked event posted THEN hide alert dialog AND openSettings`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val openSettings = viewModel.openSettings.testIn(context, name = "openSettings")
        uiState.skipItems(1)

        context.launch {
            viewModel.postUiEvent(UiEvent.ShowPermissionRationale(Manifest.permission.CAMERA))
            viewModel.postUiEvent(UiEvent.AlertDialogPositiveClicked)
        }
        uiState.skipItems(1)
        scheduler.advanceUntilIdle()

        val result = uiState.awaitItem()
        expectThat(result.alertDialogUiState).isNull()
        openSettings.expectMostRecentItem()

        uiState.cancel()
        openSettings.cancel()
    }

    companion object {

        @JvmStatic
        fun destinationActions() = listOf(
            arrayOf("Login", listOf(TopBarAction.REGISTER)),
            arrayOf("Register", emptyList<TopBarAction>()),
            arrayOf("Reports", emptyList<TopBarAction>()),
            arrayOf("CreateReport", listOf(TopBarAction.PHOTO)),
        )

        @JvmStatic
        fun destinationBackButton() = listOf(
            arrayOf("Login", false),
            arrayOf("Register", true),
            arrayOf("Reports", false),
            arrayOf("CreateReport", true),
        )

        @JvmStatic
        fun destinationFabConfig() = listOf(
            arrayOf("Login", null),
            arrayOf("Register", null),
            arrayOf(
                "Reports",
                UiState.FabConfig(
                    icon = R.drawable.ic_add_24dp,
                    contentDescription = R.string.common_button_createReport,
                ),
            ),
            arrayOf(
                "CreateReport",
                UiState.FabConfig(
                    icon = R.drawable.ic_save_24dp,
                    contentDescription = R.string.common_button_saveReport,
                ),
            ),
        )
    }
}
