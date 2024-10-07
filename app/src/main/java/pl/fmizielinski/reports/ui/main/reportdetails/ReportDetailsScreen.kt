package pl.fmizielinski.reports.ui.main.reportdetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ramcosta.composedestinations.annotation.Destination
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
            callbacks = ReportDetailsCallbacks(
                onAttachmentClicked = { postUiEvent(UiEvent.PreviewAttachment(it)) },
            ),
        )
    }
}

@Composable
fun ReportDetailsContent(
    uiState: UiState,
    callbacks: ReportDetailsCallbacks,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Margin),
        ) {
            uiState.report?.let { report ->
                if (report.attachments.isNotEmpty()) {
                    Attachments(
                        attachments = report.attachments,
                        callbacks = callbacks,
                    )
                }
                Details(report)
            }
            Comments(uiState.comments)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun Attachments(
    attachments: List<UiState.ReportDetails.Attachment>,
    callbacks: ReportDetailsCallbacks,
) {
    val carouselState = rememberCarouselState { attachments.size }
    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = 200.dp,
        itemSpacing = 16.dp,
        modifier = Modifier.padding(bottom = 16.dp),
    ) { index ->
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.clickable {
                val id = attachments[index].id
                callbacks.onAttachmentClicked(id)
            },
        ) {
            GlideImage(
                model = attachments[index].path,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun Details(report: UiState.ReportDetails) {
    Text(
        text = report.title,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
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
        modifier = Modifier.padding(top = 12.dp),
    )
}

@Composable
fun Comments(comments: List<UiState.Comment>) {
    if (comments.isNotEmpty()) {
        val state = rememberLazyListState(comments.lastIndex)
        LazyColumn(state = state) {
            items(comments) { comment ->
                Comment(comment)
            }
        }
    }
}

@Composable
fun Comment(comment: UiState.Comment) {
    val constraints = commentConstraintSet(comment.isMine)
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth(),
        constraintSet = constraints,
    ) {
        Text(
            text = comment.user,
            fontWeight = FontWeight.Light,
            fontSize = 12.sp,
            modifier = Modifier.layoutId(CommentConstraints.USER),
        )
        Card(
            modifier = Modifier.layoutId(CommentConstraints.COMMENT),
        ) {
            Text(
                text = comment.comment,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp),
            )
        }
        Text(
            text = comment.createDate,
            fontWeight = FontWeight.Light,
            fontSize = 10.sp,
            modifier = Modifier.layoutId(CommentConstraints.DATE),
        )
    }
}

private object CommentConstraints {
    const val USER = "user"
    const val COMMENT = "comment"
    const val DATE = "date"
}

private fun commentConstraintSet(isMine: Boolean): ConstraintSet {
    return ConstraintSet {
        val user = createRefFor(CommentConstraints.USER)
        val comment = createRefFor(CommentConstraints.COMMENT)
        val date = createRefFor(CommentConstraints.DATE)
        @Suppress("MagicNumber")
        val guideline = if (isMine) {
            createGuidelineFromEnd(0.6f)
        } else {
            createGuidelineFromStart(0.6f)
        }

        constrain(user) {
            top.linkTo(parent.top)
            start.linkTo(comment.start)
        }
        constrain(comment) {
            top.linkTo(user.bottom)
            if (isMine) {
                linkTo(guideline, parent.end, bias = 1f)
            } else {
                linkTo(parent.start, guideline, bias = 0f)
            }
            width = Dimension.preferredWrapContent
        }
        constrain(date) {
            top.linkTo(comment.bottom)
            end.linkTo(comment.end)
        }
    }
}

data class ReportDetailsCallbacks(
    val onAttachmentClicked: (Int) -> Unit,
)

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
private fun ReportDetailsScreenPreview() {
    ReportsTheme {
        ReportDetailsContent(
            uiState = previewUiState,
            callbacks = emptyCallbacks,
        )
    }
}

@Suppress("MaxLineLength")
private val previewUiState = UiState(
    isLoading = false,
    report = UiState.ReportDetails(
        id = 1,
        title = "Title",
        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        reportDate = "2021-01-01, 13:11",
        attachments = emptyList(),
    ),
    comments = listOf(
        UiState.Comment(
            id = 1,
            comment = "Comment 1",
            user = "User user",
            createDate = "2021-01-01, 13:11",
            isMine = true,
        ),
        UiState.Comment(
            id = 2,
            comment = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            user = "User user",
            createDate = "2021-01-01, 13:11",
            isMine = true,
        ),
        UiState.Comment(
            id = 3,
            comment = "Comment 3",
            user = "User2 user2",
            createDate = "2021-01-01, 13:11",
            isMine = false,
        ),
        UiState.Comment(
            id = 4,
            comment = "Comment 4",
            user = "User user",
            createDate = "2021-01-01, 13:11",
            isMine = true,
        ),
        UiState.Comment(
            id = 5,
            comment = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            user = "User2 user2",
            createDate = "2021-01-01, 13:11",
            isMine = false,
        ),
    ),
)

private val emptyCallbacks = ReportDetailsCallbacks(
    onAttachmentClicked = {},
)
