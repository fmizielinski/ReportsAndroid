package pl.fmizielinski.reports.ui.main.reportdetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
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
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.composable.OutlinedReportsTextField
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiState
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiState.Comment.Status
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
        val pagingContent = remember { viewModel.pagingContent }
        ReportDetailsContent(
            uiState = state.value,
            pagingContent = pagingContent,
            callbacks = ReportDetailsCallbacks(
                onAttachmentClicked = { postUiEvent(UiEvent.PreviewAttachment(it)) },
                onTabClicked = { postUiEvent(UiEvent.TabClicked(it)) },
                commentsCallbacks = CommentsCallbacks(
                    onCommentChanged = { postUiEvent(UiEvent.CommentChanged(it)) },
                    onSendClicked = { postUiEvent(UiEvent.SendClicked) },
                    onCommentClicked = { postUiEvent(UiEvent.CommentClicked(it)) },
                ),
            ),
        )
    }
}

@Composable
fun ReportDetailsContent(
    uiState: UiState,
    pagingContent: Flow<PagingData<UiState.Comment>>,
    callbacks: ReportDetailsCallbacks,
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .imePadding(),
    ) {
        val lazyPagingItems = pagingContent.collectAsLazyPagingItems()

        Tabs(
            uiState = uiState,
            onTabClicked = callbacks.onTabClicked,
        )
        if (uiState.isLoading || lazyPagingItems.loadState.refresh == LoadState.Loading) {
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
                lazyPagingItems = lazyPagingItems,
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
    lazyPagingItems: LazyPagingItems<UiState.Comment>,
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
            lazyPagingItems = lazyPagingItems,
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
    lazyPagingItems: LazyPagingItems<UiState.Comment>,
    callbacks: CommentsCallbacks,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        val state = rememberLazyListState()

        LaunchedEffect(comments.scrollToFirst, lazyPagingItems.loadState.refresh) {
            val shouldScroll = lazyPagingItems.loadState.refresh is LoadState.NotLoading ||
                    comments.scrollToFirst
            if (shouldScroll && lazyPagingItems.itemCount > 0) {
                state.scrollToItem(0)
            }
        }

        LazyColumn(
            state = state,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true,
        ) {
            items(
                count = lazyPagingItems.itemCount,
                key = lazyPagingItems.itemKey { it.id ?: -1 },
            ) { index ->
                lazyPagingItems[index]?.let { comment ->
                    if (index == 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Comment(
                        comment = comment,
                        onCommentClicked = callbacks.onCommentClicked,
                    )
                }
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
            .padding(start = 4.dp),
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
fun Comment(
    comment: UiState.Comment,
    onCommentClicked: (Int?) -> Unit,
) {
    val userWidth = remember { mutableIntStateOf(0) }
    val commentWidth = remember { mutableIntStateOf(0) }
    val dateWidth = remember { mutableIntStateOf(0) }
    val constraints = remember(userWidth.intValue, commentWidth.intValue, dateWidth.intValue) {
        mutableStateOf(
            commentConstraintSet(
                comment.isMine,
                userWidth.intValue,
                commentWidth.intValue,
                dateWidth.intValue,
            ),
        )
    }
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth(),
        constraintSet = constraints.value,
    ) {
        val cardAlpha = if (comment.status == Status.SENT) 1f else SENDING_CARD_ALPHA
        val errorBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        if (comment.status == Status.SENT) {
            Text(
                text = comment.user,
                fontWeight = FontWeight.Light,
                fontSize = 12.sp,
                modifier = Modifier.layoutId(CommentConstraints.USER)
                    .onGloballyPositioned { coordinates ->
                        userWidth.intValue = coordinates.size.width
                    },
            )
        }
        Card(
            modifier = Modifier.layoutId(CommentConstraints.COMMENT)
                .alpha(cardAlpha)
                .onGloballyPositioned { coordinates ->
                    commentWidth.intValue = coordinates.size.width
                }
                .clickable { onCommentClicked(comment.id) },
            border = errorBorder.takeIf { comment.status == Status.SENDING_FAILED },
        ) {
            Text(
                text = comment.comment,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp),
            )
        }
        CommentFooter(
            comment = comment,
            modifier = Modifier.layoutId(CommentConstraints.DATE)
                .onGloballyPositioned { coordinates ->
                    dateWidth.intValue = coordinates.size.width
                },
        )
    }
}

@Composable
fun CommentFooter(
    comment: UiState.Comment,
    modifier: Modifier,
) {
    when (comment.status) {
        Status.SENT -> Text(
            text = comment.createDate,
            fontWeight = FontWeight.Light,
            fontSize = 10.sp,
            modifier = modifier,
        )

        Status.SENDING -> CircularProgressIndicator(
            modifier = modifier
                .padding(2.dp)
                .size(12.dp),
            strokeWidth = 2.dp,
        )

        Status.SENDING_FAILED -> Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_error_24dp),
            contentDescription = stringResource(
                R.string.reportDetailsScreen_button_send,
            ),
            tint = MaterialTheme.colorScheme.error,
            modifier = modifier
                .padding(2.dp)
                .size(12.dp),
        )
    }
}

private object CommentConstraints {
    const val USER = "user"
    const val COMMENT = "comment"
    const val DATE = "date"
}

private fun commentConstraintSet(
    isMine: Boolean,
    userWidth: Int,
    commentWidth: Int,
    dateWidth: Int,
): ConstraintSet {
    return ConstraintSet {
        val user = createRefFor(CommentConstraints.USER)
        val comment = createRefFor(CommentConstraints.COMMENT)
        val date = createRefFor(CommentConstraints.DATE)

        val guideline = if (isMine) {
            createGuidelineFromEnd(COMMENT_GUIDELINE_FRACTION)
        } else {
            createGuidelineFromStart(COMMENT_GUIDELINE_FRACTION)
        }

        constrain(user) {
            top.linkTo(parent.top)
            if (userWidth > commentWidth && isMine) {
                end.linkTo(comment.end)
            } else {
                start.linkTo(comment.start)
            }
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
            if (dateWidth > commentWidth && !isMine) {
                start.linkTo(comment.start)
            } else {
                end.linkTo(comment.end)
            }
        }
    }
}

data class ReportDetailsCallbacks(
    val onAttachmentClicked: (Int) -> Unit,
    val onTabClicked: (UiState.Tab) -> Unit,
    val commentsCallbacks: CommentsCallbacks,
)

data class CommentsCallbacks(
    val onCommentChanged: (String) -> Unit,
    val onSendClicked: () -> Unit,
    val onCommentClicked: (Int?) -> Unit,
)

private const val SENDING_CARD_ALPHA = 0.5f
private const val COMMENT_GUIDELINE_FRACTION = 0.6f

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
private fun ReportDetailsScreenPreview() {
    ReportsTheme {
        ReportDetailsContent(
            uiState = previewDetailsUiState,
            pagingContent = flowOf(PagingData.empty()),
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
            pagingContent = flowOf(PagingData.from(previewComments)),
            callbacks = emptyCallbacks,
        )
    }
}

@Suppress("MaxLineLength", "StringLiteralDuplication")
@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
private fun ReportCommentsScreenSendingFailePreview() {
    ReportsTheme {
        val comments = buildList {
            val sendingComment = UiState.Comment(
                id = null,
                comment = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                user = "",
                createDate = "",
                isMine = true,
                status = Status.SENDING_FAILED,
            )
            add(sendingComment)
            addAll(previewComments)
        }
        ReportDetailsContent(
            uiState = previewCommentsUiState,
            pagingContent = flowOf(PagingData.from(comments)),
            callbacks = emptyCallbacks,
        )
    }
}

private val previewDetailsUiState = previewUiState(
    selectedTab = UiState.Tab.DETAILS,
)

@Suppress("MaxLineLength", "StringLiteralDuplication")
private val previewComments = listOf(
    UiState.Comment(
        id = 1,
        comment = "A",
        user = "User user",
        createDate = "2021-01-01, 13:11",
        isMine = true,
        status = Status.SENT,
    ),
    UiState.Comment(
        id = 2,
        comment = "A",
        user = "User2 user2",
        createDate = "2021-01-01, 13:11",
        isMine = false,
        status = Status.SENT,
    ),
    UiState.Comment(
        id = 3,
        comment = "Comment 1",
        user = "User user",
        createDate = "2021-01-01, 13:11",
        isMine = true,
        status = Status.SENT,
    ),
    UiState.Comment(
        id = 4,
        comment = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        user = "User user",
        createDate = "2021-01-01, 13:11",
        isMine = true,
        status = Status.SENT,
    ),
    UiState.Comment(
        id = 5,
        comment = "Comment 3",
        user = "User2 user2",
        createDate = "2021-01-01, 13:11",
        isMine = false,
        status = Status.SENT,
    ),
    UiState.Comment(
        id = 6,
        comment = "Comment 4",
        user = "User user",
        createDate = "2021-01-01, 13:11",
        isMine = true,
        status = Status.SENT,
    ),
)

@Suppress("MaxLineLength", "StringLiteralDuplication")
private val previewCommentsUiState = previewUiState(
    selectedTab = UiState.Tab.COMMENTS,
)

@Suppress("MaxLineLength")
private fun previewUiState(
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
        scrollToFirst = true,
    ),
    selectedTab = selectedTab,
)

private val emptyCallbacks = ReportDetailsCallbacks(
    onAttachmentClicked = {},
    onTabClicked = {},
    commentsCallbacks = CommentsCallbacks(
        onCommentChanged = {},
        onSendClicked = {},
        onCommentClicked = {},
    ),
)
