package pl.fmizielinski.reports.data.network.interceptor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.annotation.Factory

@Factory
class UnauthorizedInterceptor(
    private val unauthorizedHandler: UnauthorizedHandler,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            CoroutineScope(Dispatchers.Default).launch {
                unauthorizedHandler.postLogoutEvent()
            }
        }

        return response
    }
}

interface UnauthorizedHandler {
    suspend fun postLogoutEvent()
}
