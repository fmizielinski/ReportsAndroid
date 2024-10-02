package pl.fmizielinski.reports.ui.main.attachment.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class AttachmentGalleryNavArgs(
    val initialIndex: Int,
    val attachments: ArrayList<File>,
) : Parcelable
