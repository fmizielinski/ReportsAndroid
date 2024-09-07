package pl.fmizielinski.reports.data.network.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun File.createMultipartBody(contentType: String = "image/jpeg"): MultipartBody.Part {
    val requestFile = this.asRequestBody(contentType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("file", this.name, requestFile)
}
