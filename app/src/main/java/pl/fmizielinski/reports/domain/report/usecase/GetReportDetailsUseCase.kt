package pl.fmizielinski.reports.domain.report.usecase

import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.data.network.report.ReportService
import pl.fmizielinski.reports.domain.utils.PathProvider
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import pl.fmizielinski.reports.domain.mapper.toReportDetails
import pl.fmizielinski.reports.domain.report.model.ReportDetails

@Factory
class GetReportDetailsUseCase(
    private val reportService: ReportService,
    private val dateFormatter: DateFormatter,
    private val pathProvider: PathProvider,
) {

    suspend operator fun invoke(id: Int): ReportDetails {
        val response = reportService.getReportDetails(id)
        return response.toReportDetails(
            attachmentPath = pathProvider.getAttachmentPath(),
            dateFormatter = dateFormatter,
        )
    }
}
