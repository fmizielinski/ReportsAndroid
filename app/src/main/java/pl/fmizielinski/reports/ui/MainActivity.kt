package pl.fmizielinski.reports.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.ui.MainViewModel.UiEvent
import pl.fmizielinski.reports.ui.MainViewModel.UiState
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.consumeNavEvent
import pl.fmizielinski.reports.ui.theme.ReportsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReportsApp()
        }
    }
}

@Composable
fun ReportsApp() {
    ReportsTheme {
        BaseScreen<MainViewModel, UiState, UiEvent> {
            val navController = rememberNavController()
            val snackBarData = viewModel.showSnackBar.collectAsState(SnackBarData.empty())

            LaunchedEffect(Unit) {
                viewModel.navigationEvents.collectDestination(navController::consumeNavEvent)
            }

            MainScreen(
                navController = navController,
                snackBarData = snackBarData.value,
            )
        }
    }
}

@Composable
fun MainScreen(
    navController: NavHostController,
    snackBarData: SnackBarData = SnackBarData.empty(),
) {
    Scaffold(
        snackbarHost = {
            if (snackBarData.isValid) {
                Snackbar(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(snackBarData.messageResId!!))
                }
            }
        },
    ) {
        DestinationsNavHost(
            navGraph = NavGraphs.root,
            navController = navController,
            modifier = Modifier.fillMaxSize()
                .padding(it),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsAppPreview() {
    ReportsTheme {
        MainScreen(navController = rememberNavController())
    }
}
