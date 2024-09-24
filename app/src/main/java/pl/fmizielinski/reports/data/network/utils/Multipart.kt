package pl.fmizielinski.reports.data.network.utils

import okhttp3.MultipartBody
import java.io.File

fun File.createMultipartBody(
    contentType: String = "image/jpeg",
    progressListener: ProgressListener,
): MultipartBody.Part {
    val requestBody = ProgressRequestBody(this, contentType, progressListener)
    return MultipartBody.Part.createFormData("file", this.name, requestBody)
}
