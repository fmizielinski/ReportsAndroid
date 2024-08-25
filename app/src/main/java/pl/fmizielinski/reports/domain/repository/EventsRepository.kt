package pl.fmizielinski.reports.domain.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.koin.core.annotation.Single
import pl.fmizielinski.reports.domain.model.SnackBarData
import pl.fmizielinski.reports.ui.navigation.DestinationData
import java.util.Optional

@Single
class EventsRepository {

    private val _showSnackBar = MutableSharedFlow<SnackBarData>()
    val showSnackBar: SharedFlow<SnackBarData> = _showSnackBar

    private val _navigationEvent = MutableSharedFlow<Optional<DestinationData>>()
    val navigationEvent: SharedFlow<Optional<DestinationData>> = _navigationEvent

    suspend fun postSnackBarEvent(data: SnackBarData) {
        _showSnackBar.emit(data)
    }

    suspend fun postNavEvent(destination: DestinationData) {
        _navigationEvent.emit(Optional.of(destination))
    }

    suspend fun postNavUpEvent() {
        _navigationEvent.emit(Optional.empty())
    }
}
