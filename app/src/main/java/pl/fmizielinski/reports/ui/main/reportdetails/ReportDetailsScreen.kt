package pl.fmizielinski.reports.ui.main.reportdetails

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.composable.OutlinedReportsTextField
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
                onTabClicked = { postUiEvent(UiEvent.TabClicked(it)) },
                commentsCallbacks = CommentsCallbacks(
                    onAddAttachmentClicked = { postUiEvent(UiEvent.AddAttachmentClicked) },
                    onTextFieldFocused = { postUiEvent(UiEvent.CommentFieldFocused) },
                    onCommentChanged = { postUiEvent(UiEvent.CommentChanged(it)) },
                    onSendClicked = { postUiEvent(UiEvent.SendClicked) },
                ),
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
        Tabs(
            uiState = uiState,
            onTabClicked = callbacks.onTabClicked,
        )
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Margin,
                    end = Margin,
                    bottom = Margin,
                ),
        ) {
            TabsContent(
                uiState = uiState,
                callbacks = callbacks,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tabs(
    uiState: UiState,
    onTabClicked: (UiState.Tab) -> Unit,
) {
    SecondaryTabRow(
        selectedTabIndex = uiState.selectedTabIndex,
    ) {
        Tab(
            selected = uiState.selectedTab == UiState.Tab.DETAILS,
            onClick = { onTabClicked(UiState.Tab.DETAILS) },
            modifier = Modifier.padding(vertical = 12.dp),
        ) {
            Text(text = stringResource(R.string.reportDetailsScreen_label_details))
        }
        Tab(
            selected = uiState.selectedTab == UiState.Tab.COMMENTS,
            onClick = { onTabClicked(UiState.Tab.COMMENTS) },
            modifier = Modifier.padding(vertical = 12.dp),
        ) {
            Text(text = stringResource(R.string.reportDetailsScreen_label_comments))
        }
    }
}

@Composable
fun TabsContent(
    uiState: UiState,
    callbacks: ReportDetailsCallbacks,
) {
    when (uiState.selectedTab) {
        UiState.Tab.DETAILS -> uiState.report?.let {
            Details(
                report = it,
                onAttachmentClicked = callbacks.onAttachmentClicked,
            )
        }

        UiState.Tab.COMMENTS -> Comments(
            comments = uiState.comments,
            callbacks = callbacks.commentsCallbacks,
        )
    }
}

@Composable
fun Details(
    report: UiState.ReportDetails,
    onAttachmentClicked: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Attachments(
            attachments = report.attachments,
            onAttachmentClicked = onAttachmentClicked,
        )
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
            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun Attachments(
    attachments: List<UiState.ReportDetails.Attachment>,
    onAttachmentClicked: (Int) -> Unit,
) {
    val carouselState = rememberCarouselState { attachments.size }
    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = 200.dp,
        itemSpacing = 16.dp,
        modifier = Modifier.padding(top = Margin, bottom = 16.dp),
    ) { index ->
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.clickable {
                val id = attachments[index].id
                onAttachmentClicked(id)
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
fun Comments(
    comments: UiState.Comments,
    callbacks: CommentsCallbacks,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        val state = rememberLazyListState()

        LaunchedEffect(comments.isLoading, comments.isSending) {
            if (comments.scrollToFirst && comments.list.isNotEmpty()) {
                state.scrollToItem(comments.list.lastIndex)
            }
        }

        LazyColumn(
            state = state,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            items(comments.list) { comment ->
                Comment(comment)
            }
        }
        CommentText(callbacks)
    }
}

@Composable
fun CommentText(
    callbacks: CommentsCallbacks,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var fieldValue by remember { mutableStateOf("") }
    val onSendClicked = {
        callbacks.onSendClicked()
        fieldValue = ""
        keyboardController?.hide()
    }

    OutlinedReportsTextField(
        value = fieldValue,
        onValueChange = {
            fieldValue = it
            callbacks.onCommentChanged(it)
        },
        modifier = Modifier.fillMaxWidth()
            .padding(start = 4.dp)
            .onFocusChanged { focused ->
                if (focused.isFocused) {
                    callbacks.onTextFieldFocused()
                }
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Send,
        ),
        keyboardActions = KeyboardActions {
            onSendClicked()
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    onSendClicked()
                },
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_send_24dp),
                    contentDescription = stringResource(
                        R.string.reportDetailsScreen_button_send,
                    ),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    )
}

@Composable
fun Comment(comment: UiState.Comment) {
    val constraints = commentConstraintSet(comment.isMine)
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth(),
        constraintSet = constraints,
    ) {
        if (!comment.isSending) {
            Text(
                text = comment.user,
                fontWeight = FontWeight.Light,
                fontSize = 12.sp,
                modifier = Modifier.layoutId(CommentConstraints.USER),
            )
        }
        @Suppress("MagicNumber")
        Card(
            modifier = Modifier.layoutId(CommentConstraints.COMMENT)
                .alpha(if (comment.isSending) 0.5f else 1f),
        ) {
            Text(
                text = comment.comment,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp),
            )
        }
        if (!comment.isSending) {
            Text(
                text = comment.createDate,
                fontWeight = FontWeight.Light,
                fontSize = 10.sp,
                modifier = Modifier.layoutId(CommentConstraints.DATE),
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.layoutId(CommentConstraints.DATE)
                    .padding(2.dp)
                    .size(10.dp),
                strokeWidth = 2.dp,
            )
        }
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
    val onTabClicked: (UiState.Tab) -> Unit,
    val commentsCallbacks: CommentsCallbacks,
)

data class CommentsCallbacks(
    val onAddAttachmentClicked: () -> Unit,
    val onTextFieldFocused: () -> Unit,
    val onCommentChanged: (String) -> Unit,
    val onSendClicked: () -> Unit,
)

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
private fun ReportDetailsScreenPreview() {
    ReportsTheme {
        ReportDetailsContent(
            uiState = previewDetailsUiState,
            callbacks = emptyCallbacks,
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
private fun ReportCommentsScreenPreview() {
    ReportsTheme {
        ReportDetailsContent(
            uiState = previewCommentsUiState,
            callbacks = emptyCallbacks,
        )
    }
}

private val previewDetailsUiState = previewUiState(
    comments = emptyList(),
    selectedTab = UiState.Tab.DETAILS,
)

@Suppress("MaxLineLength", "StringLiteralDuplication")
private val previewCommentsUiState = previewUiState(
    comments = listOf(
        UiState.Comment(
            comment = "Comment 1",
            user = "User user",
            createDate = "2021-01-01, 13:11",
            isMine = true,
            isSending = false,
        ),
        UiState.Comment(
            comment = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            user = "User user",
            createDate = "2021-01-01, 13:11",
            isMine = true,
            isSending = false,
        ),
        UiState.Comment(
            comment = "Comment 3",
            user = "User2 user2",
            createDate = "2021-01-01, 13:11",
            isMine = false,
            isSending = false,
        ),
        UiState.Comment(
            comment = "Comment 4",
            user = "User user",
            createDate = "2021-01-01, 13:11",
            isMine = true,
            isSending = false,
        ),
        UiState.Comment(
            comment = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            user = "User2 user2",
            createDate = "2021-01-01, 13:11",
            isMine = false,
            isSending = false,
        ),
        UiState.Comment(
            comment = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            user = "User user",
            createDate = "2021-01-01, 13:11",
            isMine = true,
            isSending = false,
        ),
        UiState.Comment(
            comment = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            user = "User2 user2",
            createDate = "2021-01-01, 13:11",
            isMine = false,
            isSending = false,
        ),
        UiState.Comment(
            comment = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            user = "",
            createDate = "",
            isMine = true,
            isSending = true,
        ),
    ),
    selectedTab = UiState.Tab.COMMENTS,
)

@Suppress("MaxLineLength")
private fun previewUiState(
    comments: List<UiState.Comment>,
    selectedTab: UiState.Tab,
) = UiState(
    isLoading = false,
    report = UiState.ReportDetails(
        id = 1,
        title = "Title",
        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        reportDate = "2021-01-01, 13:11",
        attachments = emptyList(),
    ),
    comments = UiState.Comments(
        list = comments,
        attachmentOptionsExpanded = true,
        isLoading = false,
    ),
    selectedTab = selectedTab,
)

private val emptyCallbacks = ReportDetailsCallbacks(
    onAttachmentClicked = {},
    onTabClicked = {},
    commentsCallbacks = CommentsCallbacks(
        onAddAttachmentClicked = {},
        onTextFieldFocused = {},
        onCommentChanged = {},
        onSendClicked = {},
    ),
)
