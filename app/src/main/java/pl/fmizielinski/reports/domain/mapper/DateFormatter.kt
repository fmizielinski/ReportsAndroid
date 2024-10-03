package pl.fmizielinski.reports.domain.mapper

import org.koin.core.annotation.Factory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Factory
class DateFormatter(locale: Locale = Locale.getDefault()) {

    private val reportListDateFormatter by lazy {
        DateTimeFormatter.ofPattern("d LLL", locale)
    }
    private val reportListYearDateFormatter by lazy {
        DateTimeFormatter.ofPattern("d LLL yyyy", locale)
    }
    private val reportDetailsDateFormatter by lazy {
        DateTimeFormatter.ofPattern("d LLL yyyy, HH:mm", locale)
    }
    private val fileNameFormatter by lazy {
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", locale)
    }

    fun formatReportListDate(date: LocalDateTime, withYear: Boolean): String {
        return if (withYear) {
            reportListYearDateFormatter.format(date)
        } else {
            reportListDateFormatter.format(date)
        }
    }

    fun formatFileName(date: LocalDateTime): String {
        return fileNameFormatter.format(date)
    }

    fun formatReportDetailsDate(date: LocalDateTime): String {
        return reportDetailsDateFormatter.format(date)
    }
}
