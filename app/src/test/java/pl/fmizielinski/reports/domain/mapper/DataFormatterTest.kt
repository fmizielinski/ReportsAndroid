package pl.fmizielinski.reports.domain.mapper

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDateTime
import java.util.Locale

class DataFormatterTest {

    private val formatter = DataFormatter(Locale.US)

    @Test
    fun formatReportListDate() {
        val date = LocalDateTime.of(2021, 6, 12, 0, 0)

        val formattedDate = formatter.formatReportListDate(date)
        expectThat(formattedDate) isEqualTo "12 Jun"
    }
}
