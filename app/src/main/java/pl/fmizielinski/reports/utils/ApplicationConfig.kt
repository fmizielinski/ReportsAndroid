package pl.fmizielinski.reports.utils

import pl.fmizielinski.reports.BuildConfig

class ApplicationConfig {

    val host: String = BuildConfig.HOST
    val reportDescriptionLength: Int = BuildConfig.REPORT_DESCRIPTION_LENGTH
    val reportListPageSize: Int = BuildConfig.REPORT_LIST_PAGE_SIZE
    val reportTitleLength: Int = BuildConfig.REPORT_TITLE_LENGTH
}
