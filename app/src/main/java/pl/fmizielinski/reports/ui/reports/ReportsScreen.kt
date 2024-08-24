package pl.fmizielinski.reports.ui.reports

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import pl.fmizielinski.reports.ui.navigation.MainGraph

@Destination<MainGraph>(route = "Reports", start = true)
@Composable
fun ReportsScreen() {
    Text("Logged in")
}
