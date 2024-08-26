package pl.fmizielinski.reports.ui.reports

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.ramcosta.composedestinations.annotation.Destination
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.navigation.graph.MainGraph
import pl.fmizielinski.reports.ui.reports.ReportsViewModel.UiEvent
import pl.fmizielinski.reports.ui.reports.ReportsViewModel.UiState
import pl.fmizielinski.reports.ui.theme.ReportsTheme

@Destination<MainGraph>(route = "Reports", start = true)
@Composable
fun ReportsScreen() {
    BaseScreen<ReportsViewModel, UiState, UiEvent> {
        ReportsList(uiState = state.value)
    }
}

@Composable
fun ReportsList(
    uiState: UiState,
) {
    LazyColumn {
        itemsIndexed(uiState.reports) { index, report ->
            ReportItem(uiState = report)
            if (index != uiState.reports.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
fun ReportItem(
    uiState: UiState.Report,
) {
    ConstraintLayout(
        modifier = Modifier.padding(16.dp)
            .fillMaxWidth(),
    ) {
        val (title, description, date) = createRefs()
        Text(
            text = uiState.title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            },
        )
        Text(
            text = uiState.description,
            fontSize = 16.sp,
            modifier = Modifier.constrainAs(description) {
                top.linkTo(title.bottom)
                start.linkTo(parent.start)
            },
        )
        Text(
            text = uiState.reportDate,
            fontSize = 12.sp,
            modifier = Modifier.constrainAs(date) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
            },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun ReportsScreenPreview() {
    ReportsTheme {
        ReportsList(uiState = previewUiState)
    }
}

private val previewUiState = UiState(
    reports = listOf(
        previewReport(),
        previewReport(),
        previewReport(),
    ),
)

private fun previewReport() = UiState.Report(
    id = 1,
    title = "Title",
    description = "Description",
    reportDate = "25 sep",
)
