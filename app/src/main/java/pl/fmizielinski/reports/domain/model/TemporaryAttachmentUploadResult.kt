package pl.fmizielinski.reports.domain.model

sealed interface TemporaryAttachmentUploadResult {

    data class Progress(val progress: Float) : TemporaryAttachmentUploadResult

    data class Complete(val uuid: String) : TemporaryAttachmentUploadResult
}
