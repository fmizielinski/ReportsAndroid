package pl.fmizielinski.reports.domain.mapper

import org.koin.core.annotation.Factory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Factory
class DataFormatter(locale: Locale = Locale.getDefault()) {

    private val formatter by lazy { DateTimeFormatter.ofPattern("d LLL", locale) }

    fun formatReportListDate(date: LocalDateTime): String {
        return formatter.format(date)
    }
}
