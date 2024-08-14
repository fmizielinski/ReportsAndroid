package pl.fmizielinski.reports.domain.repository

import com.ramcosta.composedestinations.spec.Direction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.koin.core.annotation.Single
import pl.fmizielinski.reports.domain.model.SnackBarData
import java.util.Optional

@Single
class EventsRepository {
    private val _showSnackBar = MutableSharedFlow<SnackBarData>()
    val showSnackBar: SharedFlow<SnackBarData> = _showSnackBar

    private val _navigationEvent = MutableSharedFlow<Optional<Direction>>()
    val navigationEvent: SharedFlow<Optional<Direction>> = _navigationEvent

    suspend fun postSnackBarEvent(data: SnackBarData) {
        _showSnackBar.emit(data)
    }

    suspend fun postNavEvent(direction: Direction) {
        _navigationEvent.emit(Optional.of(direction))
    }

    suspend fun postNavUpEvent() {
        _navigationEvent.emit(Optional.empty())
    }
}
