package pl.fmizielinski.reports.ui.main.reportdetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import pl.fmizielinski.reports.domain.report.model.ReportDetails
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiState
import pl.fmizielinski.reports.ui.main.reportdetails.model.ReportDetailsNavArgs
import pl.fmizielinski.reports.ui.navigation.graph.MainGraph
import pl.fmizielinski.reports.ui.theme.Margin
import pl.fmizielinski.reports.ui.theme.ReportsTheme

@Destination<MainGraph>(
    route = "ReportDetails",
    navArgs = ReportDetailsNavArgs::class,
)
@Composable
fun ReportDetailsScreen() {
    BaseScreen<ReportDetailsViewModel, UiState, UiEvent> {
        ReportDetailsContent(
            uiState = state.value,
        )
    }
}

@Composable
fun ReportDetailsContent(uiState: UiState) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(Margin),
        ) {
            if (uiState.report != null) {
                Details(uiState.report)
            }
        }
    }
}

@Composable
fun Details(report: ReportDetails) {
    Text(
        text = report.title,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
    )
    Text(
        text = report.reportDate,
        fontWeight = FontWeight.Light,
        fontSize = 10.sp,
    )
    Text(
        text = report.description,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    )
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
private fun ReportDetailsScreenPreview() {
    ReportsTheme {
        ReportDetailsContent(previewUiState)
    }
}

@Suppress("MaxLineLength")
private val previewUiState = UiState(
    isLoading = false,
    report = ReportDetails(
        id = 1,
        title = "Title",
        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        reportDate = "2021-01-01, 13:11",
        attachments = emptyList(),
    ),
)
