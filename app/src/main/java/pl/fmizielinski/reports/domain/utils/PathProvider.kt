package pl.fmizielinski.reports.domain.utils

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.BuildConfig

@Factory
class PathProvider {

    private fun getHost(): String = BuildConfig.HOST

    fun getAttachmentPath() = "${getHost()}/report/attachment/"
}
