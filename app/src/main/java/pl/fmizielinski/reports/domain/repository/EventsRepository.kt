package pl.fmizielinski.reports.domain.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.koin.core.annotation.Single
import pl.fmizielinski.reports.data.network.interceptor.UnauthorizedHandler
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.ui.navigation.DestinationData
import java.io.File
import java.util.Optional

@Single
class EventsRepository : UnauthorizedHandler {

    private val _showSnackBar = MutableSharedFlow<SnackBarData>()
    val showSnackBar: SharedFlow<SnackBarData> = _showSnackBar

    private val _navigationEvent = MutableSharedFlow<Optional<DestinationData>>()
    val navigationEvent: SharedFlow<Optional<DestinationData>> = _navigationEvent

    private val _globalEvent = MutableSharedFlow<GlobalEvent>()
    val globalEvent: SharedFlow<GlobalEvent> = _globalEvent

    suspend fun postSnackBarEvent(data: SnackBarData) {
        _showSnackBar.emit(data)
    }

    suspend fun postNavEvent(destination: DestinationData) {
        _navigationEvent.emit(Optional.of(destination))
    }

    suspend fun postNavUpEvent() {
        _navigationEvent.emit(Optional.empty())
    }

    override suspend fun postLogoutEvent() {
        postGlobalEvent(GlobalEvent.Logout)
    }

    suspend fun postGlobalEvent(event: GlobalEvent) {
        _globalEvent.emit(event)
    }

    sealed interface GlobalEvent {
        data object Login : GlobalEvent
        data object Logout : GlobalEvent
        data object Register : GlobalEvent
        data object SaveReport : GlobalEvent
        data class ChangeFabVisibility(val isVisible: Boolean) : GlobalEvent
        data class AddAttachment(val photoFile: File) : GlobalEvent
        data class Loading(val isLoading: Boolean) : GlobalEvent
    }
}
