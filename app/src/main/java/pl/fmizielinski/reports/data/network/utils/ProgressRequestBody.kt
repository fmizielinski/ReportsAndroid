package pl.fmizielinski.reports.data.network.utils

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.buffer
import okio.source
import pl.fmizielinski.reports.utils.roundToDecimalPlaces
import timber.log.Timber
import java.io.File

class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val progressListener: ProgressListener,
) : RequestBody() {

    override fun contentType(): MediaType? = contentType.toMediaTypeOrNull()

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val fileLength = file.length().toFloat()
        Timber.d("Attachment body size: $fileLength")
        val source = file.source().buffer()
        var uploaded = 0L

        source.use { buffered ->
            var read: Long
            while (buffered.read(sink.buffer, DEFAULT_BUFFER_SIZE).also { read = it } != -1L) {
                uploaded += read
                sink.flush()
                val progress = uploaded / fileLength
                Timber.d("Attachment body uploaded: $uploaded of $fileLength, Progress: $progress")
                progressListener(progress.roundToDecimalPlaces(2))
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048L
    }
}

typealias ProgressListener = (progress: Float) -> Unit
