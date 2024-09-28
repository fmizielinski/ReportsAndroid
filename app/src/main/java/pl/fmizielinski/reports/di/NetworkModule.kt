package pl.fmizielinski.reports.di

import android.content.Context
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import pl.fmizielinski.reports.BuildConfig
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.auth.AuthService
import pl.fmizielinski.reports.data.network.interceptor.BearerInterceptor
import pl.fmizielinski.reports.data.network.interceptor.UnauthorizedInterceptor
import pl.fmizielinski.reports.data.network.report.ReportService
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Module
@ComponentScan("pl.fmizielinski.reports.data.network")
class NetworkModule {

    @Single
    @Named(NO_AUTH_RETROFIT)
    fun noAuthRetrofit(
        @Named(JSON_CONVERTER_FACTORY) jsonConverterFactory: Converter.Factory,
        @Named(NO_AUTH_CLIENT) client: OkHttpClient,
    ): Retrofit = retrofit(jsonConverterFactory, client)

    @Single
    @Named(BEARER_RETROFIT)
    fun bearerRetrofit(
        @Named(JSON_CONVERTER_FACTORY) jsonConverterFactory: Converter.Factory,
        @Named(BEARER_CLIENT) client: OkHttpClient,
    ): Retrofit = retrofit(jsonConverterFactory, client)

    fun retrofit(
        jsonConverterFactory: Converter.Factory,
        client: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.HOST)
        .addConverterFactory(jsonConverterFactory)
        .client(client)
        .build()

    @Single
    fun authService(@Named(NO_AUTH_RETROFIT) retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Single
    @Named(BEARER_AUTH_SERVICE)
    fun bearerAuthService(@Named(BEARER_RETROFIT) retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Single
    fun reportService(@Named(BEARER_RETROFIT) retrofit: Retrofit): ReportService =
        retrofit.create(ReportService::class.java)

    @Factory
    @Named(NO_AUTH_CLIENT)
    fun noAuthOkHttpClient(
        sslSocketFactory: SSLSocketFactory,
        trustManager: X509TrustManager,
        hostnameVerifier: HostnameVerifier,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustManager)
        .hostnameVerifier(hostnameVerifier)
        .addInterceptor(loggingInterceptor)
        .build()

    @Factory
    @Named(BEARER_CLIENT)
    fun bearerOkHttpClient(
        sslSocketFactory: SSLSocketFactory,
        trustManager: X509TrustManager,
        hostnameVerifier: HostnameVerifier,
        loggingInterceptor: HttpLoggingInterceptor,
        bearerInterceptor: BearerInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustManager)
        .hostnameVerifier(hostnameVerifier)
        .addInterceptor(bearerInterceptor)
        .addInterceptor(unauthorizedInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Factory
    @Named(JSON_CONVERTER_FACTORY)
    fun jsonConverterFactory(json: Json): Converter.Factory = json.asConverterFactory(
        "application/json; charset=UTF8".toMediaType(),
    )

    @Factory
    fun json(): Json = Json { ignoreUnknownKeys = true }

    @Single
    fun trustManager(context: Context): X509TrustManager {
        val cf = CertificateFactory.getInstance("X.509")
        val caInput: InputStream = context.resources.openRawResource(R.raw.certificate)
        val ca = cf.generateCertificate(caInput)

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("reports", ca)

        caInput.close()
        return TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            .apply { init(keyStore) }
            .trustManagers
            .first { it is X509TrustManager } as X509TrustManager
    }

    @Factory
    fun sslSocketFactory(trustManager: X509TrustManager): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        return sslContext.socketFactory
    }

    @Factory
    fun hostnameVerifier(): HostnameVerifier = HostnameVerifier { hostname, _ ->
        BuildConfig.HOST.contains(hostname)
    }

    @Factory
    fun loggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    companion object {
        const val NO_AUTH_RETROFIT = "noAuthRetrofit"
        const val JSON_CONVERTER_FACTORY = "jsonConverterFactory"
        const val NO_AUTH_CLIENT = "noAuthClient"
        const val BEARER_RETROFIT = "bearerRetrofit"
        const val BEARER_CLIENT = "bearerClient"
        const val BEARER_AUTH_SERVICE = "bearerAuthService"
    }
}
