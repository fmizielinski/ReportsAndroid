package pl.fmizielinski.reports.domain.model

sealed interface AttachmentUploadResult {

    data class Progress(val progress: Float) : AttachmentUploadResult

    data object Complete : AttachmentUploadResult
}
