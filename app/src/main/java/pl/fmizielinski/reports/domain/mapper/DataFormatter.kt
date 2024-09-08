package pl.fmizielinski.reports.domain.mapper

import org.koin.core.annotation.Factory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Factory
class DataFormatter(locale: Locale = Locale.getDefault()) {

    private val reportListDateFormatter by lazy {
        DateTimeFormatter.ofPattern("d LLL", locale)
    }
    private val fileNameFormatter by lazy {
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", locale)
    }

    fun formatReportListDate(date: LocalDateTime): String {
        return reportListDateFormatter.format(date)
    }

    fun formatFileName(date: LocalDateTime): String {
        return fileNameFormatter.format(date)
    }
}
