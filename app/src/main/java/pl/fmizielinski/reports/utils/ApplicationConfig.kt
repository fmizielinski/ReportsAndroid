package pl.fmizielinski.reports.utils

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.BuildConfig

@Factory
class ApplicationConfig {

    val host: String = BuildConfig.HOST
    val reportDescriptionLength: Int = BuildConfig.REPORT_DESCRIPTION_LENGTH
    val reportListPageSize: Int = BuildConfig.REPORT_LIST_PAGE_SIZE
    val reportTitleLength: Int = BuildConfig.REPORT_TITLE_LENGTH
}
