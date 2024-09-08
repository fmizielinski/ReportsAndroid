package pl.fmizielinski.reports.ui.main.reports

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.reports.ReportsViewModel.UiState
import pl.fmizielinski.reports.ui.navigation.graph.MainGraph
import pl.fmizielinski.reports.ui.theme.Margin
import pl.fmizielinski.reports.ui.theme.ReportsTheme

@Destination<MainGraph>(route = "Reports", start = true)
@Composable
fun ReportsScreen() {
    BaseScreen<ReportsViewModel, UiState, UiEvent> {
        ReportsList(
            uiState = state.value,
            callbacks = ReportsCallbacks(
                onListScrolled = { firstItemIndex ->
                    coroutineScope.launch {
                        viewModel.postUiEvent(UiEvent.ListScrolled(firstItemIndex))
                    }
                },
            ),
        )
    }
}

@Composable
fun ReportsList(
    uiState: UiState,
    callbacks: ReportsCallbacks,
) {
    if (uiState.reports.isEmpty()) {
        EmptyListPlaceholder()
    } else {
        ReportsListContent(
            uiState = uiState,
            callbacks = callbacks,
        )
    }
}

@Composable
fun ReportsListContent(
    uiState: UiState,
    callbacks: ReportsCallbacks,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect(callbacks.onListScrolled)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(uiState.reports) { index, report ->
            ReportItem(uiState = report)
            if (index != uiState.reports.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = Margin),
                )
            }
        }
    }
}

@Composable
fun EmptyListPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(Margin),
    ) {
        Text(
            text = stringResource(R.string.reportsScreen_label_emptyList),
            fontWeight = FontWeight.Light,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
fun ReportItem(
    uiState: UiState.Report,
) {
    ConstraintLayout(
        modifier = Modifier.padding(Margin)
            .fillMaxWidth(),
    ) {
        val title = createRef()
        val description = createRef()
        val date = createRef()
        val comments = createRef()
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
        CommentsIndicator(
            comments = uiState.comments,
            modifier = Modifier.constrainAs(comments) {
                top.linkTo(description.bottom)
                end.linkTo(parent.end)
            },
        )
    }
}

@Composable
fun CommentsIndicator(
    comments: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
    ) {
        Text(
            text = comments.toString(),
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 4.dp)
                .padding(end = 4.dp)
                .align(Alignment.CenterVertically),
        )
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_comment_24dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(14.dp)
                .align(Alignment.CenterVertically),
        )
    }
}

data class ReportsCallbacks(
    val onListScrolled: (firstItemIndex: Int) -> Unit,
)

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun ReportsScreenPreview() {
    ReportsTheme {
        ReportsList(
            uiState = previewUiState,
            callbacks = emptyCallbacks,
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun ReportsScreenEmptyPreview() {
    ReportsTheme {
        ReportsList(
            uiState = previewUiStateEmpty,
            callbacks = emptyCallbacks,
        )
    }
}

private val previewUiState = UiState(
    reports = listOf(
        previewReport(),
        previewReport(),
        previewReport(),
    ),
)

private val previewUiStateEmpty = UiState(
    reports = emptyList(),
)

private val emptyCallbacks = ReportsCallbacks(
    onListScrolled = {},
)

private fun previewReport() = UiState.Report(
    id = 1,
    title = "Title",
    description = "Description",
    reportDate = "25 sep",
    comments = 3,
)
