package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.report.model.AddReportAttachmentData
import pl.fmizielinski.reports.domain.report.model.AddTemporaryAttachmentData
import java.io.File

fun addReportAttachmentData(
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
