package pl.fmizielinski.reports.ui

import app.cash.turbine.testIn
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
import pl.fmizielinski.reports.domain.common.model.SnackBarData
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.auth.usecase.IsLoggedInUseCase
import pl.fmizielinski.reports.domain.auth.usecase.LogoutUseCase
import pl.fmizielinski.reports.fixtures.ui.alertDialogUiState
import pl.fmizielinski.reports.ui.MainViewModel.UiEvent
import pl.fmizielinski.reports.ui.common.model.FabUiState
import pl.fmizielinski.reports.ui.common.model.TopBarAction
import pl.fmizielinski.reports.ui.common.model.TopBarNavigationIcon
import pl.fmizielinski.reports.ui.destinations.destinations.CreateReportDestination
import pl.fmizielinski.reports.ui.destinations.destinations.LoginDestination
import pl.fmizielinski.reports.ui.destinations.destinations.RegisterDestination
import pl.fmizielinski.reports.ui.destinations.destinations.ReportsDestination
import pl.fmizielinski.reports.ui.destinations.navgraphs.AuthNavGraph
import pl.fmizielinski.reports.ui.destinations.navgraphs.MainNavGraph
import pl.fmizielinski.reports.ui.navigation.toDestinationData
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.io.File

@ExperimentalCoroutinesApi
class MainViewModelTest : BaseViewModelTest<MainViewModel, UiEvent>() {
    private val eventsRepository = spyk(EventsRepository())
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
        navigationIcon: TopBarNavigationIcon?,
    ) = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns false

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        postUiEvent(UiEvent.NavDestinationChanged(destination))
        uiState.skipItems(1)

        val result = uiState.awaitItem()
        expectThat(result.appBarUiState.navigationIcon) isEqualTo navigationIcon

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
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        postUiEvent(UiEvent.NavDestinationChanged(destination))
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
        fabUiState: FabUiState?,
    ) = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns false

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        postUiEvent(UiEvent.NavDestinationChanged(destination))
        uiState.skipItems(1)

        val result = uiState.awaitItem()
        expectThat(result.fabUiState) isEqualTo fabUiState

        uiState.cancel()
        navigationEvents.cancel()
    }

    @Test
    fun `WHEN Back button is clicked THEN Navigate up`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        postUiEvent(UiEvent.BackClicked)

        expectThat(navigationEvents.awaitItem().isPresent).isFalse()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN REGISTER action is clicked THEN Navigate to RegisterScreen`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        postUiEvent(UiEvent.ActionClicked(TopBarAction.REGISTER))

        expectThat(navigationEvents.awaitItem().get()) isEqualTo RegisterDestination.toDestinationData()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN PHOTO action is clicked THEN take picture`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val takePicture = viewModel.takePicture.testIn(context, name = "takePicture")

        postUiEvent(UiEvent.ActionClicked(TopBarAction.PHOTO))
        scheduler.advanceUntilIdle()

        takePicture.expectMostRecentItem()

        takePicture.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN FILES action is clicked THEN pick file`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val pickFile = viewModel.pickFile.testIn(context, name = "pickFile")

        postUiEvent(UiEvent.ActionClicked(TopBarAction.FILES))
        scheduler.advanceUntilIdle()

        pickFile.expectMostRecentItem()

        pickFile.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN LOGOUT action is clicked THEN show logout alert`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")

        postUiEvent(UiEvent.ActionClicked(TopBarAction.LOGOUT))
        scheduler.advanceUntilIdle()

        val result = uiState.expectMostRecentItem()
        expectThat(result.alertDialogUiState).isNotNull()
            .and {
                get { iconResId } isEqualTo R.drawable.ic_help_24dp
                get { titleResId } isEqualTo R.string.common_label_logout
                get { messageResId } isEqualTo R.string.common_label_logoutQuestion
                get { positiveButtonResId } isEqualTo R.string.common_label_yes
                get { negativeButtonResId } isEqualTo R.string.common_label_no
            }

        uiState.cancel()
    }

    @Test
    fun `GIVEN logout alert is visible WHEN AlertDialogPositiveClicked THEN Navigate to AuthNavGraph start destination`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")
        coJustRun { logoutUseCase() }

        postUiEvent(UiEvent.ActionClicked(TopBarAction.LOGOUT))
        scheduler.advanceUntilIdle()
        postUiEvent(UiEvent.AlertDialogPositiveClicked)
        scheduler.advanceUntilIdle()

        val result = navigationEvents.expectMostRecentItem().get()
        expectThat(result) isEqualTo AuthNavGraph.startDestination.toDestinationData()

        uiState.cancelAndIgnoreRemainingEvents()
        navigationEvents.cancel()
    }

    @Test
    fun `GIVEN user is logged in WHEN app starts THEN navigate to MainNavGraph start destination`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns true

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        postUiEvent(UiEvent.NavDestinationChanged(AuthNavGraph.startDestination.route))

        expectThat(navigationEvents.awaitItem().get()) isEqualTo MainNavGraph.startDestination.toDestinationData()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN user is not logged in WHEN app starts THEN Don't navigate further`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns false

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        postUiEvent(UiEvent.NavDestinationChanged(AuthNavGraph.startDestination.route))

        navigationEvents.expectNoEvents()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN Current destination is Reports WHEN fab clicked THEN Navigate to CreateReport`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns true

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        postUiEvent(UiEvent.NavDestinationChanged(LoginDestination.baseRoute))
        postUiEvent(UiEvent.NavDestinationChanged(ReportsDestination.baseRoute))
        navigationEvents.skipItems(1)
        postUiEvent(UiEvent.FabClicked)
        navigationEvents.skipItems(1)
        scheduler.advanceUntilIdle()

        expectThat(navigationEvents.awaitItem().get()) isEqualTo CreateReportDestination.toDestinationData()

        navigationEvents.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN Current destination is CreateReport WHEN fab clicked THEN Post save event`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns true

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        postUiEvent(UiEvent.NavDestinationChanged(LoginDestination.baseRoute))
        postUiEvent(UiEvent.NavDestinationChanged(CreateReportDestination.baseRoute))
        postUiEvent(UiEvent.FabClicked)
        scheduler.advanceUntilIdle()
        uiState.skipItems(3)

        coVerify(exactly = 1) { eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.SaveReport) }

        navigationEvents.cancelAndIgnoreRemainingEvents()
        uiState.cancel()
    }

    @Test
    fun `GIVEN Current destination is CreateReport AND fab is hidden WHEN ChangeFabVisibility event posted THEN show fab`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns true

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val navigationEvents = viewModel.navigationEvents.testIn(context, name = "navigationEvents")

        postUiEvent(UiEvent.NavDestinationChanged(LoginDestination.baseRoute))
        postUiEvent(UiEvent.NavDestinationChanged(CreateReportDestination.baseRoute))
        postUiEvent(UiEvent.FabClicked)
        scheduler.advanceUntilIdle()
        context.launch {
            eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.ChangeFabVisibility(isVisible = true))
        }
        scheduler.advanceUntilIdle()

        expectThat(uiState.expectMostRecentItem().fabUiState) isEqualTo FabUiState(
            icon = R.drawable.ic_save_24dp,
            contentDescription = R.string.common_button_saveReport,
        )
        coVerify(exactly = 1) { eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.SaveReport) }

        navigationEvents.cancelAndIgnoreRemainingEvents()
        uiState.cancel()
    }

    @Test
    fun `WHEN PictureTaken event posted THEN post AddAttachment global event`() = runTurbineTest {
        val file = File.createTempFile("test", "jpg")

        val uiState = viewModel.uiState.testIn(context, name = "uiState")

        postUiEvent(UiEvent.PictureTaken(file))
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.AddAttachment(file)) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN TakePictureFailed event posted THEN post snackbar event`() = runTurbineTest {
        val snackBarData = SnackBarData(messageResId = R.string.common_error_oops)

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val showSnackBar = viewModel.showSnackBar.testIn(context, name = "showSnackBar")

        postUiEvent(UiEvent.TakePictureFailed)
        scheduler.advanceUntilIdle()

        expectThat(showSnackBar.awaitItem()) isEqualTo snackBarData
        expectThat(showSnackBar.awaitItem()) isEqualTo SnackBarData.empty()

        uiState.cancelAndIgnoreRemainingEvents()
        showSnackBar.cancel()
    }

    @Test
    fun `WHEN FilePicked event posted THEN post AddAttachment global event`() = runTurbineTest {
        val file = File.createTempFile("test", "jpg")

        val uiState = viewModel.uiState.testIn(context, name = "uiState")

        postUiEvent(UiEvent.FilePicked(file))
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.AddAttachment(file)) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `WHEN PickFileFailed event posted THEN post snackbar event`() = runTurbineTest {
        val snackBarData = SnackBarData(messageResId = R.string.common_error_oops)

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val showSnackBar = viewModel.showSnackBar.testIn(context, name = "showSnackBar")

        postUiEvent(UiEvent.PickFileFailed)
        scheduler.advanceUntilIdle()

        expectThat(showSnackBar.awaitItem()) isEqualTo snackBarData
        expectThat(showSnackBar.awaitItem()) isEqualTo SnackBarData.empty()

        uiState.cancelAndIgnoreRemainingEvents()
        showSnackBar.cancel()
    }

    @Test
    fun `WHEN PHOTO ShowPermissionRationale event posted THEN show alert dialog`() = runTurbineTest {
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

        postUiEvent(UiEvent.ShowPermissionRationale(TopBarAction.PHOTO))

        val result = uiState.awaitItem()
        expectThat(result.alertDialogUiState) isEqualTo expected

        uiState.cancel()
    }

    @Test
    fun `WHEN FILES ShowPermissionRationale event posted THEN show alert dialog`() = runTurbineTest {
        val expectedIconResId = R.drawable.ic_info_24dp
        val expectedTitleResId = R.string.common_label_permission
        val expectedMessageResId = R.string.common_label_imagesPermissionRationale
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

        postUiEvent(UiEvent.ShowPermissionRationale(TopBarAction.FILES))

        val result = uiState.awaitItem()
        expectThat(result.alertDialogUiState) isEqualTo expected

        uiState.cancel()
    }

    @Test
    fun `GIVEN alert dialog visible WHEN AlertDialogDismissed event posted THEN hide alert dialog`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        uiState.skipItems(1)

        postUiEvent(UiEvent.ShowPermissionRationale(TopBarAction.PHOTO))
        postUiEvent(UiEvent.AlertDialogDismissed)
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

        postUiEvent(UiEvent.ShowPermissionRationale(TopBarAction.PHOTO))
        postUiEvent(UiEvent.AlertDialogPositiveClicked)
        uiState.skipItems(1)
        scheduler.advanceUntilIdle()

        val result = uiState.awaitItem()
        expectThat(result.alertDialogUiState).isNull()
        openSettings.expectMostRecentItem()

        uiState.cancel()
        openSettings.cancel()
    }

    @Test
    fun `WHEN Loading event posted THEN disable app bar`() = runTurbineTest {
        coEvery { isLoggedInUseCase() } returns true

        val uiState = viewModel.uiState.testIn(context, name = "uiState")

        context.launch {
            eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.Loading(isLoading = true))
        }
        scheduler.advanceUntilIdle()

        expectThat(uiState.expectMostRecentItem().appBarUiState.isEnabled).isFalse()

        uiState.cancel()
    }

    companion object {

        @JvmStatic
        fun destinationActions() = listOf(
            arrayOf("Login", listOf(TopBarAction.REGISTER)),
            arrayOf("Register", emptyList<TopBarAction>()),
            arrayOf("Reports", listOf(TopBarAction.LOGOUT)),
            arrayOf("CreateReport", arrayListOf(TopBarAction.FILES, TopBarAction.PHOTO)),
            arrayOf("AttachmentGallery", emptyList<TopBarAction>()),
        )

        @JvmStatic
        fun destinationBackButton() = listOf(
            arrayOf("Login", null),
            arrayOf("Register", TopBarNavigationIcon.BACK),
            arrayOf("Reports", null),
            arrayOf("CreateReport", TopBarNavigationIcon.BACK),
            arrayOf("AttachmentGallery", TopBarNavigationIcon.CLOSE),
        )

        @JvmStatic
        fun destinationFabConfig() = listOf(
            arrayOf(
                "Login",
                FabUiState(
                    icon = R.drawable.ic_login_24dp,
                    contentDescription = R.string.common_button_login,
                ),
            ),
            arrayOf(
                "Register",
                FabUiState(
                    icon = R.drawable.ic_person_add_24dp,
                    contentDescription = R.string.common_button_register,
                ),
            ),
            arrayOf(
                "Reports",
                FabUiState(
                    icon = R.drawable.ic_add_24dp,
                    contentDescription = R.string.common_button_createReport,
                ),
            ),
            arrayOf(
                "CreateReport",
                FabUiState(
                    icon = R.drawable.ic_save_24dp,
                    contentDescription = R.string.common_button_saveReport,
                ),
            ),
        )
    }
}
