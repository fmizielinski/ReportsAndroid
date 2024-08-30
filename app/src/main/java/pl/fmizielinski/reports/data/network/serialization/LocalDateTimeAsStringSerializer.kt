package pl.fmizielinski.reports.data.network.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAsStringSerializer : KSerializer<LocalDateTime> {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSS")

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "LocalDateTime",
        kind = PrimitiveKind.STRING,
    )

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(formatter.format(value))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}

typealias LocalDateTimeAsString = @Serializable(LocalDateTimeAsStringSerializer::class) LocalDateTime
