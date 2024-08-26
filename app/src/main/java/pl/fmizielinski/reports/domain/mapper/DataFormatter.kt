package pl.fmizielinski.reports.domain.mapper

import org.koin.core.annotation.Factory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Factory
class DataFormatter {

    private val formatter by lazy { DateTimeFormatter.ofPattern("dd LLL") }

    fun formatReportListDate(date: LocalDateTime): String {
        return formatter.format(date)
    }
}
