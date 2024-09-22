package pl.fmizielinski.reports.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.composedestinations.utils.startDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.koinInject
import pl.fmizielinski.reports.android.CustomActivityResultContracts
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.ui.MainViewModel.UiEvent
import pl.fmizielinski.reports.ui.MainViewModel.UiState
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.composable.AlertDialog
import pl.fmizielinski.reports.ui.common.composable.AlertDialogCallbacks
import pl.fmizielinski.reports.ui.common.composable.ReportsTopAppBar
import pl.fmizielinski.reports.ui.common.composable.ReportsTopAppBarCallbacks
import pl.fmizielinski.reports.ui.common.composable.emptyAlertDialogCallbacks
import pl.fmizielinski.reports.ui.common.composable.emptyTopAppBarCallbacks
import pl.fmizielinski.reports.ui.common.composable.previewTopAppBarUiState
import pl.fmizielinski.reports.ui.common.consumeNavEvent
import pl.fmizielinski.reports.ui.theme.ReportsTheme
import pl.fmizielinski.reports.ui.utils.FileUtils
import java.io.File

@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { viewModel.isInitialLoading.value }
        enableEdgeToEdge()
        setContent {
            ReportsApp()
        }
    }
}

@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
@Composable
fun ReportsApp() {
    ReportsTheme {
        BaseScreen<MainViewModel, UiState, UiEvent> {
            val navController = rememberNavController()
            viewModel.handleNavigationEvents(coroutineScope, navController)

            val snackBarData = viewModel.showSnackBar.collectAsState(SnackBarData.empty())

            viewModel.handleTakePicture(coroutineScope)
            viewModel.handlePickFile(coroutineScope)
            viewModel.handleOpenSettings()

            MainScreen(
                uiState = state.value,
                navController = navController,
                snackBarData = snackBarData.value,
                callbacks = MainCallbacks(
                    onFabClicked = { postUiEvent(UiEvent.FabClicked) },
                    topAppBarCallbacks = ReportsTopAppBarCallbacks(
                        onBackClicked = { postUiEvent(UiEvent.BackClicked) },
                        onActionClicked = { postUiEvent(UiEvent.ActionClicked(it)) },
                        onShouldShowPermissionRationale = {
                            postUiEvent(UiEvent.ShowPermissionRationale(it))
                        },
                    ),
                ),
                alertDialogCallbacks = AlertDialogCallbacks(
                    onDismissRequest = { postUiEvent(UiEvent.AlertDialogDismissed) },
                    onNegativeClick = { postUiEvent(UiEvent.AlertDialogDismissed) },
                    onPositiveClick = { postUiEvent(UiEvent.AlertDialogPositiveClicked) },
                ),
            )
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun MainViewModel.handleOpenSettings() {
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = CustomActivityResultContracts.OpenAppSettings(),
    ) { _ -> }

    LaunchedEffect(Unit) {
        openSettings.collect {
            settingsLauncher.launch()
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun MainViewModel.handleTakePicture(scope: CoroutineScope) {
    val context = LocalContext.current
    var photoFile by remember { mutableStateOf<File?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val fileUtils = koinInject<FileUtils>()

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        scope.launch {
            if (success) {
                postUiEvent(UiEvent.PictureTaken(requireNotNull(photoFile)))
            } else {
                postUiEvent(UiEvent.TakePictureFailed)
            }
        }
    }

    LaunchedEffect(Unit) {
        takePicture.collect {
            photoFile = fileUtils.createPhotoFile(context)
            photoUri = fileUtils.getUriForFile(context, requireNotNull(photoFile))

            takePictureLauncher.launch(requireNotNull(photoUri))
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun MainViewModel.handlePickFile(scope: CoroutineScope) {
    val context = LocalContext.current
    val fileUtils = koinInject<FileUtils>()

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        scope.launch {
            if (uri == null) {
                postUiEvent(UiEvent.PickFileFailed)
            } else {
                val file = fileUtils.getFileForUri(context, uri)
                postUiEvent(UiEvent.FilePicked(file))
            }
        }
    }

    LaunchedEffect(Unit) {
        pickFile.collect {
            val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

            pickFileLauncher.launch(request)
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun MainViewModel.handleNavigationEvents(scope: CoroutineScope, navController: NavHostController) {
    val currentDestination = navController.currentDestinationAsState().value
        ?: NavGraphs.reports.startDestination
    val navigator: DestinationsNavigator = navController.rememberDestinationsNavigator()

    LaunchedEffect(Unit) {
        navigationEvents.collectDestination(navigator::consumeNavEvent)
    }
    LaunchedEffect(currentDestination) {
        scope.launch {
            val event = UiEvent.NavDestinationChanged(currentDestination.baseRoute)
            postUiEvent(event)
        }
    }
}


@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
@Composable
fun MainScreen(
    uiState: UiState,
    navController: NavHostController,
    snackBarData: SnackBarData = SnackBarData.empty(),
    callbacks: MainCallbacks,
    alertDialogCallbacks: AlertDialogCallbacks,
) {
    Scaffold(
        topBar = {
            ReportsTopAppBar(
                uiState = uiState.appBarUiState,
                callbacks = callbacks.topAppBarCallbacks,
            )
        },
        snackbarHost = {
            if (snackBarData.isValid) {
                Snackbar(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(snackBarData.messageResId!!))
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.fabConfig != null,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                Fab(
                    config = uiState.fabConfig,
                    onFabClicked = callbacks.onFabClicked,
                )
            }
        },
    ) {
        DestinationsNavHost(
            navGraph = NavGraphs.reports,
            navController = navController,
            modifier = Modifier.fillMaxWidth()
                .padding(it),
        )

        if (uiState.alertDialogUiState != null) {
            AlertDialog(
                uiState = uiState.alertDialogUiState,
                callbacks = alertDialogCallbacks,
            )
        }
    }
}

@Composable
fun Fab(
    config: UiState.FabConfig?,
    onFabClicked: () -> Unit,
) {
    FloatingActionButton(
        onClick = onFabClicked,
        content = {
            if (config != null) {
                Icon(
                    imageVector = ImageVector.vectorResource(config.icon),
                    contentDescription = stringResource(config.contentDescription),
                )
            }
        },
    )
}

data class MainCallbacks(
    val onFabClicked: () -> Unit,
    val topAppBarCallbacks: ReportsTopAppBarCallbacks,
)

@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun ReportsAppPreview() {
    ReportsTheme {
        MainScreen(
            uiState = previewUiState,
            navController = rememberNavController(),
            callbacks = emptyCallbacks,
            alertDialogCallbacks = emptyAlertDialogCallbacks,
        )
    }
}

private val previewUiState = UiState(
    appBarUiState = previewTopAppBarUiState,
    fabConfig = null,
    alertDialogUiState = null,
)

private val emptyCallbacks = MainCallbacks(
    onFabClicked = {},
    topAppBarCallbacks = emptyTopAppBarCallbacks,
)
