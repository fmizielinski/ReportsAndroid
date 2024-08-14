package pl.fmizielinski.reports.domain.repository

import app.cash.turbine.test
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import kotlinx.coroutines.test.runTest
import org.junit.Test
import pl.fmizielinski.reports.domain.model.SnackBarData
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class EventsRepositoryTest {
    private val eventsRepository = EventsRepository()

    @Test
    fun postNavEvent() =
        runTest {
            eventsRepository.navigationEvent.test {
                eventsRepository.postNavEvent(LoginScreenDestination())

                expectThat(awaitItem().get()) isEqualTo LoginScreenDestination()
                cancel()
            }
        }

    @Test
    fun postNavUpEvent() =
        runTest {
            eventsRepository.navigationEvent.test {
                eventsRepository.postNavUpEvent()

                expectThat(awaitItem().isPresent) isEqualTo false
                cancel()
            }
        }

    @Test
    fun postSnackBarEvent() =
        runTest {
            eventsRepository.showSnackBar.test {
                eventsRepository.postSnackBarEvent(SnackBarData.empty())

                expectThat(awaitItem()) isEqualTo SnackBarData.empty()
                cancel()
            }
        }
}
