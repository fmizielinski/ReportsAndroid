package pl.fmizielinski.reports.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.composedestinations.utils.startDestination
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.ui.MainViewModel.UiEvent
import pl.fmizielinski.reports.ui.MainViewModel.UiState
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.consumeNavEvent
import pl.fmizielinski.reports.ui.model.TopBarAction
import pl.fmizielinski.reports.ui.theme.ReportsTheme

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

@ExperimentalMaterial3Api
@Composable
fun ReportsApp() {
    ReportsTheme {
        BaseScreen<MainViewModel, UiState, UiEvent> {
            val navController = rememberNavController()
            val currentDestination = navController.currentDestinationAsState().value
                ?: NavGraphs.reports.startDestination
            val navigator: DestinationsNavigator = navController.rememberDestinationsNavigator()

            val snackBarData = viewModel.showSnackBar.collectAsState(SnackBarData.empty())

            LaunchedEffect(Unit) {
                viewModel.navigationEvents.collectDestination(navigator::consumeNavEvent)
            }
            LaunchedEffect(currentDestination) {
                coroutineScope.launch {
                    val event = UiEvent.NavDestinationChanged(currentDestination.baseRoute)
                    viewModel.postUiEvent(event)
                }
            }

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

@ExperimentalMaterial3Api
@Composable
fun ReportsTopBar(
    uiState: UiState,
    callbacks: MainCallbacks,
) {
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
                IconButton(
                    onClick = { callbacks.onActionClicked(action) },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(action.iconResId),
                        contentDescription = stringResource(action.nameResId),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
    )
}

data class MainCallbacks(
    val onBackClicked: () -> Unit,
    val onActionClicked: (TopBarAction) -> Unit,
    val onFabClicked: () -> Unit,
)

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
