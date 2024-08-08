package pl.fmizielinski.reports.di

import kotlinx.serialization.json.Json
import okhttp3.MediaType
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import pl.fmizielinski.reports.BuildConfig
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
class NetworkModule {
    @Single
    fun retrofit(
        @Named("jsonConverterFactory") jsonConverterFactory: Converter.Factory,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.HOST)
        .addConverterFactory(jsonConverterFactory)
        .build()

    @Single
    @Named("jsonConverterFactory")
    fun jsonConverterFactory(): Converter.Factory =
        Json.asConverterFactory(
            MediaType.get("application/json; charset=UTF8"),
        )
}
