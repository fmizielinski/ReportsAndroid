package pl.fmizielinski.reports.domain.report.model

sealed interface TemporaryAttachmentUploadResult {

    data class Progress(val progress: Float) : TemporaryAttachmentUploadResult

    data class Complete(val uuid: String) : TemporaryAttachmentUploadResult
}
