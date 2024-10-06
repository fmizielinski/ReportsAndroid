package pl.fmizielinski.reports.fixtures.data

import pl.fmizielinski.reports.data.network.report.model.CreateReportResponseModel

fun createReportResponse(
    id: Int = 1,
) = CreateReportResponseModel(
    id = id,
)
