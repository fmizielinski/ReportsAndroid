package pl.fmizielinski.reports

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pl.fmizielinski.reports.ui.theme.ReportsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReportsApplication()
        }
    }
}

@Composable
fun ReportsApp() {
    ReportsTheme {
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsAppPreview() {
    ReportsTheme {
    }
}
