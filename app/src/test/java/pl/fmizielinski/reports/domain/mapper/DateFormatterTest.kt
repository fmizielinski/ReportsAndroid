package pl.fmizielinski.reports.domain.mapper

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDateTime
import java.util.Locale

class DateFormatterTest {

    private val formatter = DateFormatter(Locale.US)

    @Test
    fun formatReportListDate() {
        val date = LocalDateTime.of(2021, 6, 12, 0, 0)

        val formattedDate = formatter.formatReportListDate(date, isCurrentYear = false)
        expectThat(formattedDate) isEqualTo "12 Jun"
    }

    @Test
    fun formatReportListDate_withYear() {
        val date = LocalDateTime.of(2021, 6, 12, 0, 0)

        val formattedDate = formatter.formatReportListDate(date, isCurrentYear = true)
        expectThat(formattedDate) isEqualTo "12 Jun 2021"
    }

    @Test
    fun formatFileName() {
        val date = LocalDateTime.of(2021, 6, 12, 11, 30, 11)

        val formattedDate = formatter.formatFileName(date)
        expectThat(formattedDate) isEqualTo "20210612_113011"
    }

    @Test
    fun formatReportDetailsDate() {
        val date = LocalDateTime.of(2021, 6, 12, 11, 30, 11)

        val formattedDate = formatter.formatReportDetailsDate(date)
        expectThat(formattedDate) isEqualTo "12 Jun 2021, 11:30"
    }
}
