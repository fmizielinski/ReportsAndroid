package pl.fmizielinski.reports.domain.report.model

sealed interface AttachmentUploadResult {

    data class Progress(val progress: Float) : AttachmentUploadResult

    data object Complete : AttachmentUploadResult
}
