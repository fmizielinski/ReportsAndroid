package pl.fmizielinski.reports.domain.utils

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.utils.ApplicationConfig

@Factory
class PathProvider(
    private val config: ApplicationConfig,
) {

    private fun getHost(): String = config.host

    fun getAttachmentPath() = "${getHost()}/report/attachment/"
}
