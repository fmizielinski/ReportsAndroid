package pl.fmizielinski.reports.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.ui.MainViewModel.UiEvent
import pl.fmizielinski.reports.ui.MainViewModel.UiState
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.consumeNavEvent
import pl.fmizielinski.reports.ui.model.TopBarAction
import pl.fmizielinski.reports.ui.theme.ReportsTheme
import pl.fmizielinski.reports.ui.utils.FileUtils
import pl.fmizielinski.reports.ui.utils.requestPermission
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

            MainScreen(
                uiState = state.value,
                navController = navController,
                snackBarData = snackBarData.value,
                callbacks = MainCallbacks(
                    onBackClicked = {
                        coroutineScope.launch { viewModel.postUiEvent(UiEvent.BackClicked) }
                    },
                    onActionClicked = {
                        coroutineScope.launch { viewModel.postUiEvent(UiEvent.ActionClicked(it)) }
                    },
                    onFabClicked = {
                        coroutineScope.launch { viewModel.postUiEvent(UiEvent.FabClicked) }
                    },
                ),
            )
        }
    }
}

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
                postUiEvent(UiEvent.PictureTaken(requireNotNull(photoUri)))
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
) {
    Scaffold(
        topBar = {
            ReportsTopBar(
                uiState = uiState,
                callbacks = callbacks,
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
            if (uiState.fabConfig != null) {
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
    }
}

@Composable
fun Fab(
    config: UiState.FabConfig,
    onFabClicked: () -> Unit,
) {
    FloatingActionButton(
        onClick = onFabClicked,
        content = {
            Icon(
                imageVector = ImageVector.vectorResource(config.icon),
                contentDescription = stringResource(config.contentDescription),
            )
        },
    )
}

@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
@Composable
fun ReportsTopBar(
    uiState: UiState,
    callbacks: MainCallbacks,
) {
    val coroutineScope = rememberCoroutineScope()
    CenterAlignedTopAppBar(
        title = {
            if (uiState.title != null) {
                Text(text = stringResource(uiState.title))
            }
        },
        navigationIcon = {
            if (uiState.isBackVisible) {
                IconButton(
                    onClick = callbacks.onBackClicked,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.common_button_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        actions = {
            uiState.actions.forEach { action ->
                if (action.requirePermission != null) {
                    requestPermission(
                        permission = action.requirePermission,
                        onGrantedCallback = { callbacks.onActionClicked(action) },
                    ) { onClick ->
                        Action(
                            action = action,
                            onActionClicked = { onClick() },
                        )
                    }
                } else {
                    Action(
                        action = action,
                        onActionClicked = callbacks.onActionClicked,
                    )
                }
            }
        },
    )
}

@ExperimentalPermissionsApi
@Composable
fun Action(
    action: TopBarAction,
    onActionClicked: ((TopBarAction) -> Unit)? = null,
) {
    IconButton(
        onClick = { onActionClicked?.invoke(action) },
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(action.iconResId),
            contentDescription = stringResource(action.nameResId),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

data class MainCallbacks(
    val onBackClicked: () -> Unit,
    val onActionClicked: (TopBarAction) -> Unit,
    val onFabClicked: () -> Unit,
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
        )
    }
}

private val previewUiState = UiState(
    actions = emptyList(),
    isBackVisible = false,
    title = null,
    fabConfig = null,
)

private val emptyCallbacks = MainCallbacks(
    onBackClicked = {},
    onActionClicked = {},
    onFabClicked = {},
)
