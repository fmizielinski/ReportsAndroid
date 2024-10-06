package pl.fmizielinski.reports.domain.auth.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.data.db.dao.TokenDao
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class IsLoggedInUseCaseTest {

    private val tokenDao: TokenDao = mockk()

    private val useCase = IsLoggedInUseCase(tokenDao)

    @Test
    fun `GIVEN token exists WHEN check if logged in THEN return true`() = runTest {
        coEvery { tokenDao.hasToken() } returns true

        val result = useCase()
        expectThat(result).isTrue()
    }

    @Test
    fun `GIVEN token does not exist WHEN check if logged in THEN return false`() = runTest {
        coEvery { tokenDao.hasToken() } returns false

        val result = useCase()
        expectThat(result).isFalse()
    }
}