package pl.fmizielinski.reports.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.db.dao.TokenDao

@Factory
class BearerInterceptor(
    private val tokenDao: TokenDao,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenDao.getTokenBlocking()
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${token.token}")
            .build()
        return chain.proceed(newRequest)
    }
}
