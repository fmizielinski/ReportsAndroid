package pl.fmizielinski.reports.domain.usecase.report

import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.fixtures.domain.attachmentData
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.io.File

class GetAttachmentGalleryNavArgsTest {

    private val useCase = GetAttachmentGalleryNavArgsUseCase()

    @Test
    fun `WHEN invoke THEN return nav args`() {
        val localId1 = 1
        val localId2 = 2
        val file1 = File.createTempFile("file1", "jpg")
        val file2 = File.createTempFile("file2", "jpg")
        val attachments = listOf(
            attachmentData(localId1, file1),
            attachmentData(localId2, file2),
        )

        val result = useCase(localId2, attachments)
        expectThat(result) {
            get { initialIndex } isEqualTo 1
            get { this.attachments }.hasSize(2)
                .contains(file1.absolutePath, file2.absolutePath)
        }
    }
}
