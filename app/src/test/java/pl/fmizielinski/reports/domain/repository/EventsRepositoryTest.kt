package pl.fmizielinski.reports.domain.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.domain.common.model.SnackBarData
import pl.fmizielinski.reports.ui.destinations.destinations.LoginDestination
import pl.fmizielinski.reports.ui.navigation.toDestinationData
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class EventsRepositoryTest {
    private val eventsRepository = EventsRepository()

    @Test
    fun postNavEvent() = runTest {
        eventsRepository.navigationEvent.test {
            val destinationData = LoginDestination.toDestinationData()
            eventsRepository.postNavEvent(destinationData)

            expectThat(awaitItem().get()) isEqualTo destinationData
            cancel()
        }
    }

    @Test
    fun postNavUpEvent() = runTest {
        eventsRepository.navigationEvent.test {
            eventsRepository.postNavUpEvent()

            expectThat(awaitItem().isPresent) isEqualTo false
            cancel()
        }
    }

    @Test
    fun postSnackBarEvent() = runTest {
        eventsRepository.showSnackBar.test {
            eventsRepository.postSnackBarEvent(SnackBarData.empty())

            expectThat(awaitItem()) isEqualTo SnackBarData.empty()
            cancel()
        }
    }

    @Test
    fun postLogoutEvent() = runTest {
        eventsRepository.globalEvent.test {
            eventsRepository.postLogoutEvent()

            expectThat(awaitItem()) isEqualTo EventsRepository.GlobalEvent.Logout
            cancel()
        }
    }

    @Test
    fun postGlobalEvent() = runTest {
        eventsRepository.globalEvent.test {
            eventsRepository.postGlobalEvent(EventsRepository.GlobalEvent.SaveReport)

            expectThat(awaitItem()) isEqualTo EventsRepository.GlobalEvent.SaveReport
            cancel()
        }
    }
}
