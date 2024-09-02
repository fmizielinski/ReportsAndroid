package pl.fmizielinski.reports.data.network.report

import pl.fmizielinski.reports.data.network.report.model.CreateReportRequest
import pl.fmizielinski.reports.data.network.report.model.ReportsResponseModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ReportService {

    @GET("/report")
    @Headers("Content-Type: application/json")
    suspend fun getReports(): ReportsResponseModel

    @POST("/report")
    @Headers("Accept: application/json")
    suspend fun createReport(@Body request: CreateReportRequest)
}
