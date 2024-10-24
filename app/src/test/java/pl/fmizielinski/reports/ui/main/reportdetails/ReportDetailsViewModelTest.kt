package pl.fmizielinski.reports.ui.main.reportdetails

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.testIn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.report.usecase.AddCommentUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetCommentsUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetReportDetailsAttachmentGalleryNavArgsUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetReportDetailsUseCase
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.fixtures.domain.comment
import pl.fmizielinski.reports.fixtures.domain.reportDetails
import pl.fmizielinski.reports.fixtures.domain.reportDetailsAttachment
import pl.fmizielinski.reports.fixtures.domain.simpleErrorException
import pl.fmizielinski.reports.fixtures.ui.attachmentGalleryNavArgs
import pl.fmizielinski.reports.ui.destinations.destinations.AttachmentGalleryDestination
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiState.Comment.Status
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiState.Tab
import pl.fmizielinski.reports.ui.navigation.DestinationData
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import strikt.assertions.withFirst

class ReportDetailsViewModelTest : BaseViewModelTest<ReportDetailsViewModel, UiEvent>() {

    private val id = 1
    private val handle = SavedStateHandle(
        mapOf("id" to id),
    )

    private val eventsRepository = spyk(EventsRepository())
    private val getReportDetailsUseCase: GetReportDetailsUseCase = mockk()
    private val getAttachmentGalleryNavArgsUseCase: GetReportDetailsAttachmentGalleryNavArgsUseCase = mockk()
    private val getCommentsUseCase: GetCommentsUseCase = mockk()
    private val addCommentUseCase: AddCommentUseCase = mockk()

    override fun createViewModel(dispatcher: TestDispatcher): ReportDetailsViewModel {
        return ReportDetailsViewModel(
            dispatcher = dispatcher,
            handle = handle,
            eventsRepository = eventsRepository,
            getReportDetailsUseCase = getReportDetailsUseCase,
            getAttachmentGalleryNavArgsUseCase = getAttachmentGalleryNavArgsUseCase,
            getCommentsUseCase = getCommentsUseCase,
            addCommentUseCase = addCommentUseCase,
        )
    }

    @Test
    fun `WHEN start THEN show report details`() = runTurbineTest {
        val expectedId = id
        val expectedTitle = "title"
        val expectedDescription = "description"
        val expectedReportDate = "12 Jun 2021, 12:00"
        val expectedAttachmentId = 2
        val expectedPath = "path"
        val attachments = listOf(
            reportDetailsAttachment(
                id = expectedAttachmentId,
                path = expectedPath,
            ),
        )
        val reportDetails = reportDetails(
            id = expectedId,
            title = expectedTitle,
            description = expectedDescription,
            reportDate = expectedReportDate,
            attachments = attachments,
        )
        val commentId = 3
        val comment = "comment"
        val user = "user"
        val createDate = "createDate"
        val isMine = true

        coEvery { getReportDetailsUseCase(expectedId) } returns reportDetails
        coEvery { getCommentsUseCase(id) } returns listOf(
            comment(
                id = commentId,
                comment = comment,
                user = user,
                createDate = createDate,
                isMine = isMine,
            ),
        )

        val uiState = viewModel.uiState.testIn(context)

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()

        val result = uiState.expectMostRecentItem()
        expectThat(result) {
            get { report }.isNotNull()
                .and {
                    get { this.id } isEqualTo expectedId
                    get { title } isEqualTo expectedTitle
                    get { description } isEqualTo expectedDescription
                    get { reportDate } isEqualTo expectedReportDate
                    get { attachments }.hasSize(1)
                        .withFirst {
                            get { id } isEqualTo expectedAttachmentId
                            get { path } isEqualTo expectedPath
                        }
                }
            get { comments }.and {
                get { list }.hasSize(1)
                    .withFirst {
                        get { this.id } isEqualTo commentId
                        get { this.comment } isEqualTo comment
                        get { this.user } isEqualTo user
                        get { this.createDate } isEqualTo createDate
                        get { this.isMine } isEqualTo isMine
                        get { this.status } isEqualTo Status.SENT
                    }
                get { this.isSending }.isFalse()
                get { this.scrollToFirst }.isTrue()
            }
            get { selectedTabIndex } isEqualTo 0
        }

        uiState.cancel()
    }

    @Test
    fun `GIVEN details loading error WHEN start THEN post snack bar event AND post nav up event`() = runTurbineTest {
        val exception = simpleErrorException()
        coEvery { getReportDetailsUseCase(id) } throws exception
        coEvery { getCommentsUseCase(id) } returns emptyList()

        val uiState = viewModel.uiState.testIn(context)

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postNavUpEvent() }
        coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(exception.toSnackBarData()) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN comments loading error WHEN start THEN post snack bar event AND post nav up event`() = runTurbineTest {
        val exception = simpleErrorException()
        coEvery { getReportDetailsUseCase(id) } returns reportDetails()
        coEvery { getCommentsUseCase(id) } throws exception

        val uiState = viewModel.uiState.testIn(context)

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postNavUpEvent() }
        coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(exception.toSnackBarData()) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN has attachments WHEN PreviewAttachment event posted THEH post AttachmentGallery navigation event`() = runTurbineTest {
        val path = "path"
        val attachmentGalleryNavArgs = attachmentGalleryNavArgs(
            initialIndex = 1,
            attachments = arrayListOf(path),
        )
        val attachmentId = 2
        val reportDetails = reportDetails(
            attachments = listOf(reportDetailsAttachment(id = attachmentId)),
        )
        every { getAttachmentGalleryNavArgsUseCase(any(), any()) } returns attachmentGalleryNavArgs
        coEvery { getReportDetailsUseCase(id) } returns reportDetails
        coEvery { getCommentsUseCase(id) } returns emptyList()

        val uiState = viewModel.uiState.testIn(context, name = "uiState")

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()

        postUiEvent(UiEvent.PreviewAttachment(attachmentId))
        scheduler.advanceUntilIdle()
        val expectedDirection = AttachmentGalleryDestination(attachmentGalleryNavArgs)
        val directionSlot = slot<DestinationData>()
        coVerify(exactly = 1) { eventsRepository.postNavEvent(capture(directionSlot)) }
        expectThat(directionSlot.captured.direction.route) isEqualTo expectedDirection.route

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `GIVEN comment typed WHEN Send clicked THEN show sending comment on list`() = runTurbineTest {
        val reportDetails = reportDetails()
        val commentId = 3
        val comment = "comment"
        val user = "user"
        val createDate = "createDate"
        val isMine = true
        coEvery { getReportDetailsUseCase(id) } returns reportDetails
        coEvery { getCommentsUseCase(id) } returns emptyList()
        coEvery { addCommentUseCase(any(), any()) } coAnswers {
            delay(10000L)
            comment(
                id = commentId,
                comment = comment,
                user = user,
                createDate = createDate,
                isMine = isMine,
            )
        }

        val uiState = viewModel.uiState.testIn(context, name = "uiState")

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()
        postUiEvent(UiEvent.TabClicked(Tab.COMMENTS))
        postUiEvent(UiEvent.CommentChanged("comment"))
        postUiEvent(UiEvent.SendClicked)
        scheduler.advanceTimeBy(5000L)
        var result = uiState.expectMostRecentItem()
        expectThat(result.comments) {
            get { isSending }.isTrue()
            get { scrollToFirst }.isTrue()
            get { list }.hasSize(1)
                .withFirst {
                    get { this.id }.isNull()
                    get { this.comment } isEqualTo comment
                    get { this.user }.isEmpty()
                    get { this.createDate }.isEmpty()
                    get { this.isMine } isEqualTo isMine
                    get { this.status } isEqualTo Status.SENDING
                }
        }
        scheduler.advanceUntilIdle()
        result = uiState.expectMostRecentItem()
        expectThat(result.comments) {
            get { isSending }.isFalse()
            get { scrollToFirst }.isTrue()
            get { list }.hasSize(1)
                .withFirst {
                    get { this.id } isEqualTo commentId
                    get { this.comment } isEqualTo comment
                    get { this.user } isEqualTo user
                    get { this.createDate } isEqualTo createDate
                    get { this.isMine } isEqualTo isMine
                    get { this.status } isEqualTo Status.SENT
                }
        }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN add comment error WHEN Send clicked THEN show comment error on list`() = runTurbineTest {
        val reportDetails = reportDetails()
        val comment = "comment"
        val isMine = true
        coEvery { getReportDetailsUseCase(id) } returns reportDetails
        coEvery { getCommentsUseCase(id) } returns emptyList()
        coEvery { addCommentUseCase(any(), any()) } throws simpleErrorException()

        val uiState = viewModel.uiState.testIn(context, name = "uiState")

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()
        postUiEvent(UiEvent.TabClicked(Tab.COMMENTS))
        postUiEvent(UiEvent.CommentChanged("comment"))
        postUiEvent(UiEvent.SendClicked)
        scheduler.advanceUntilIdle()

        val result = uiState.expectMostRecentItem()
        expectThat(result.comments) {
            get { isSending }.isFalse()
            get { scrollToFirst }.isTrue()
            get { list }.hasSize(1)
                .withFirst {
                    get { this.id }.isNull()
                    get { this.comment } isEqualTo comment
                    get { this.user }.isEmpty()
                    get { this.createDate }.isEmpty()
                    get { this.isMine } isEqualTo isMine
                    get { this.status } isEqualTo Status.SENDING_FAILED
                }
        }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN add comment error visible WHEN Comment clicked THEN retry adding comment`() = runTurbineTest {
        val reportDetails = reportDetails()
        coEvery { getReportDetailsUseCase(id) } returns reportDetails
        coEvery { getCommentsUseCase(id) } returns emptyList()
        coEvery { addCommentUseCase(any(), any()) } throws simpleErrorException()

        val uiState = viewModel.uiState.testIn(context, name = "uiState")

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()
        postUiEvent(UiEvent.TabClicked(Tab.COMMENTS))
        postUiEvent(UiEvent.CommentChanged("comment"))
        postUiEvent(UiEvent.SendClicked)
        scheduler.advanceUntilIdle()
        postUiEvent(UiEvent.CommentClicked(id = null))
        scheduler.advanceUntilIdle()

        coVerify(exactly = 2) { addCommentUseCase(any(), any()) }

        uiState.cancelAndIgnoreRemainingEvents()
    }
}
