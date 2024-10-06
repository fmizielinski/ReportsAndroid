package pl.fmizielinski.reports.utils

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import pl.fmizielinski.reports.di.NetworkModule
import java.io.InputStream

@GlideModule
class ReportsGlideModule : AppGlideModule(), KoinComponent {
    private val okHttpClient: OkHttpClient by inject(named(NetworkModule.BEARER_CLIENT))

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val factory = OkHttpUrlLoader.Factory(okHttpClient)
        registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }
}
