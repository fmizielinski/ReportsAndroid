package pl.fmizielinski.reports.ui.utils

import android.content.Context
import android.net.Uri
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

    companion object {
        const val PHOTO_FILE_EXTENSION = "jpg"
        const val FILE_PROVIDER_AUTHORITY = "pl.fmizielinski.reports.provider"
    }
}
