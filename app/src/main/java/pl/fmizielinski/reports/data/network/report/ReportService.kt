package pl.fmizielinski.reports.data.network.report

import okhttp3.MultipartBody
import pl.fmizielinski.reports.data.network.report.model.AddCommentRequestModel
import pl.fmizielinski.reports.data.network.report.model.AddTemporaryAttachmentResponseModel
import pl.fmizielinski.reports.data.network.report.model.CommentsResponseModel
import pl.fmizielinski.reports.data.network.report.model.CreateReportRequestModel
import pl.fmizielinski.reports.data.network.report.model.CreateReportResponseModel
import pl.fmizielinski.reports.data.network.report.model.ReportDetailsResponseModel
import pl.fmizielinski.reports.data.network.report.model.ReportsResponseModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportService {

    // region report

    @GET("/report")
    @Headers("Content-Type: application/json")
    suspend fun getReports(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): ReportsResponseModel

    @POST("/report")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json",
    )
    suspend fun createReport(@Body request: CreateReportRequestModel): CreateReportResponseModel

    @GET("/report/{id}")
    @Headers("Content-Type: application/json")
    suspend fun getReportDetails(@Path("id") id: Int): ReportDetailsResponseModel

    // endregion report

    // region attachment

    @Multipart
    @POST("/report/{id}/attachment")
    suspend fun addAttachment(
        @Path("id") id: Int,
        @Part attachment: MultipartBody.Part,
    )

    @Multipart
    @POST("/report/attachment")
    suspend fun addTemporaryAttachment(
        @Part attachment: MultipartBody.Part,
    ): AddTemporaryAttachmentResponseModel

    // endregion attachment

    // region comment

    @GET("/report/{id}/comment")
    suspend fun getComments(
        @Path("id") reportId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): CommentsResponseModel

    @POST("/report/{id}/comment")
    suspend fun addComment(
        @Path("id") reportId: Int,
        @Body requestModel: AddCommentRequestModel,
    ): CommentsResponseModel.CommentModel

    // endregion comment
}
