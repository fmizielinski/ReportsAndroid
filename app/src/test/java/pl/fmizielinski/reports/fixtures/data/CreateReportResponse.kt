package pl.fmizielinski.reports.fixtures.data

import pl.fmizielinski.reports.data.network.report.model.CreateReportResponse

fun createReportResponse(
    id: Int = 1,
) = CreateReportResponse(
    id = id,
)
