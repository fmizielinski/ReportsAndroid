package pl.fmizielinski.reports.data.network.report

import pl.fmizielinski.reports.data.network.report.model.ReportsResponseModel
import retrofit2.http.GET
import retrofit2.http.Headers

interface ReportService {

    @GET("/report")
    @Headers("Content-Type: application/json")
    suspend fun getReports(): ReportsResponseModel
}
