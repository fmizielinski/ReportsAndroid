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
    private val commentTodayDateFormatter by lazy {
        DateTimeFormatter.ofPattern("HH:mm", locale)
    }
    private val commentWeekDateFormatter by lazy {
        DateTimeFormatter.ofPattern("EEEE, HH:mm", locale)
    }
    private val commentDateFormatter by lazy {
        DateTimeFormatter.ofPattern("d LLL, HH:mm", locale)
    }
    private val commentYearDateFormatter by lazy {
        DateTimeFormatter.ofPattern("d LLL yyyy, HH:mm", locale)
    }

    fun formatReportListDate(date: LocalDateTime, isCurrentYear: Boolean): String {
        return if (isCurrentYear) {
            reportListDateFormatter.format(date)
        } else {
            reportListYearDateFormatter.format(date)
        }
    }

    fun formatFileName(date: LocalDateTime): String {
        return fileNameFormatter.format(date)
    }

    fun formatReportDetailsDate(date: LocalDateTime): String {
        return reportDetailsDateFormatter.format(date)
    }

    fun formatCommentDate(
        date: LocalDateTime,
        isToday: Boolean = false,
        isCurrentWeek: Boolean = false,
        isCurrentYear: Boolean = false,
    ): String = when {
        isToday -> commentTodayDateFormatter.format(date)
        isCurrentWeek -> commentWeekDateFormatter.format(date)
        isCurrentYear -> commentDateFormatter.format(date)
        else -> commentYearDateFormatter.format(date)
    }
}
