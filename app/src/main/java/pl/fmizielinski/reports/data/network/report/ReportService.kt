package pl.fmizielinski.reports.data.network.report

import okhttp3.MultipartBody
import pl.fmizielinski.reports.data.network.report.model.CreateReportRequest
import pl.fmizielinski.reports.data.network.report.model.CreateReportResponse
import pl.fmizielinski.reports.data.network.report.model.ReportsResponseModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ReportService {

    @GET("/report")
    @Headers("Content-Type: application/json")
    suspend fun getReports(): ReportsResponseModel

    @POST("/report")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json",
    )
    suspend fun createReport(@Body request: CreateReportRequest): CreateReportResponse

    @Multipart
    @POST("/report/{id}/attachment")
    suspend fun addAttachment(
        @Path("id") id: Int,
        @Part attachment: MultipartBody.Part,
    )
}
