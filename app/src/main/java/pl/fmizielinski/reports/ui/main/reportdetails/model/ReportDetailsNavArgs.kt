package pl.fmizielinski.reports.ui.main.reportdetails.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReportDetailsNavArgs(
    val id: Int,
) : Parcelable
