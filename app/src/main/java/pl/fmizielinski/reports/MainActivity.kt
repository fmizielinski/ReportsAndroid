package pl.fmizielinski.reports

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
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
        val navController = rememberNavController()
        MainScreen(navController = navController)
    }
}

@Composable
fun MainScreen(
    navController: NavHostController,
) {
    DestinationsNavHost(
        navGraph = NavGraphs.root,
        navController = navController,
    )
}

@Preview(showBackground = true)
@Composable
fun ReportsAppPreview() {
    ReportsTheme {
        MainScreen(navController = rememberNavController())
    }
}
