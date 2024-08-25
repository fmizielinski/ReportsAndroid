package pl.fmizielinski.reports.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
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
                    onRegisterClicked = {
                        coroutineScope.launch { viewModel.postUiEvent(UiEvent.RegisterClicked) }
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
                callbacks = callbacks
            )
        },
        snackbarHost = {
            if (snackBarData.isValid) {
                Snackbar(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(snackBarData.messageResId!!))
                }
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

@ExperimentalMaterial3Api
@Composable
fun ReportsTopBar(
    uiState: UiState,
    callbacks: MainCallbacks,
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            if (uiState.isBackVisible) {
                IconButton(
                    onClick = callbacks.onBackClicked,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back_24dp),
                        contentDescription = stringResource(R.string.common_button_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        actions = {
            uiState.actions.forEach { action ->
                when (action) {
                    TopBarAction.REGISTER -> {
                        TextButton(
                            onClick = callbacks.onRegisterClicked,
                        ) {
                            Text(
                                text = stringResource(action.nameResId),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        },
    )
}

data class MainCallbacks(
    val onBackClicked: () -> Unit,
    val onRegisterClicked: () -> Unit,
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
)

private val emptyCallbacks = MainCallbacks(
    onBackClicked = {},
    onRegisterClicked = {},
)
