package pl.fmizielinski.reports.ui.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.domain.mapper.DateFormatter
import java.io.File
import java.time.LocalDateTime

@Factory
class FileUtils(private val dateFormatter: DateFormatter) {

    fun createPhotoFile(context: Context): File {
        val date = LocalDateTime.now()
        val fileName = "${dateFormatter.formatFileName(date)}.$PHOTO_FILE_EXTENSION"
        return File(context.filesDir, fileName)
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            FILE_PROVIDER_AUTHORITY,
            file,
        )
    }

    fun getFileForUri(context: Context, uri: Uri): File {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = it.getString(columnIndex)
            }
        }
        return File(requireNotNull(filePath))
    }

    companion object {
        const val PHOTO_FILE_EXTENSION = "jpg"
        const val FILE_PROVIDER_AUTHORITY = "pl.fmizielinski.reports.provider"
    }
}
