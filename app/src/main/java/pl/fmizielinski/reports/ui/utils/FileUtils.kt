package pl.fmizielinski.reports.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import org.koin.core.annotation.Factory
import pl.fmizielinski.reports.domain.mapper.DataFormatter
import java.io.File
import java.time.LocalDateTime

@Factory
class FileUtils(private val dateFormatter: DataFormatter) {

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

    @Suppress("DEPRECATION")
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, uri),
            )
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    companion object {
        const val PHOTO_FILE_EXTENSION = ".jpg"
        const val FILE_PROVIDER_AUTHORITY = "pl.fmizielinski.reports.provider"
    }
}
