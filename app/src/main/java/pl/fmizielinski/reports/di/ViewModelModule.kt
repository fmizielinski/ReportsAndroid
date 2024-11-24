package pl.fmizielinski.reports.di

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.annotation.KoinViewModel
import org.koin.core.Koin
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.parameter.parametersOf
import pl.fmizielinski.reports.domain.report.usecase.AddCommentUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetCommentsUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetReportDetailsAttachmentGalleryNavArgsUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetReportDetailsUseCase
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.ui.destinations.destinations.ReportDetailsDestination
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel

@Module
@ComponentScan("pl.fmizielinski.reports.ui")
class ViewModelModule {

    @Single
    fun dispatcher(): CoroutineDispatcher = Dispatchers.Default

    @KoinViewModel
    fun reportDetailsViewModel(
        koin: Koin,
        dispatcher: CoroutineDispatcher,
        handle: SavedStateHandle,
        eventsRepository: EventsRepository,
        getReportDetailsUseCase: GetReportDetailsUseCase,
        getAttachmentGalleryNavArgsUseCase: GetReportDetailsAttachmentGalleryNavArgsUseCase,
        addCommentUseCase: AddCommentUseCase,
    ): ReportDetailsViewModel {
        val reportId = ReportDetailsDestination.argsFrom(handle).id
        val getCommentsUseCase: GetCommentsUseCase = koin.get { parametersOf(reportId) }
        return ReportDetailsViewModel(
            dispatcher,
            handle,
            eventsRepository,
            getReportDetailsUseCase,
            getAttachmentGalleryNavArgsUseCase,
            getCommentsUseCase,
            addCommentUseCase,
        )
    }
}
