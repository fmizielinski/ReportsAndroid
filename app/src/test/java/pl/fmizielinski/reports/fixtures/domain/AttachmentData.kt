package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.model.AddReportAttachmentData
import pl.fmizielinski.reports.domain.model.AddTemporaryAttachmentData
import java.io.File

fun addAttachmentData(
    reportId: Int,
    file: File,
) = AddReportAttachmentData(
    reportId = reportId,
    file = file,
)

fun addTemporaryAttachmentData(
    file: File,
) = AddTemporaryAttachmentData(
    file = file,
)
