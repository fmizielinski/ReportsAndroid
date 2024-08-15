package pl.fmizielinski.reports.di

import android.content.Context
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import pl.fmizielinski.reports.BuildConfig
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.data.network.auth.AuthService
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
class NetworkModule {
    @Single
    fun retrofit(
        @Named("jsonConverterFactory") jsonConverterFactory: Converter.Factory,
        client: OkHttpClient,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.HOST)
            .addConverterFactory(jsonConverterFactory)
            .client(client)
            .build()

    @Single
    fun authService(retrofit: Retrofit): AuthService = retrofit.create(AuthService::class.java)

    @Factory
    fun okHttpClient(
        sslSocketFactory: SSLSocketFactory,
        trustManager: X509TrustManager,
        hostnameVerifier: HostnameVerifier,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier(hostnameVerifier)
            .addInterceptor(loggingInterceptor)
            .build()

    @Factory
    @Named("jsonConverterFactory")
    fun jsonConverterFactory(): Converter.Factory =
        Json.asConverterFactory(
            "application/json; charset=UTF8".toMediaType(),
        )

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
    fun hostnameVerifier(): HostnameVerifier =
        HostnameVerifier { hostname, _ ->
            BuildConfig.HOST.contains(hostname)
        }

    @Factory
    fun loggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
}
