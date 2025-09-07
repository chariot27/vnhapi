package br.ars.vnhapi.shared.utils

// Converter para tags (List<String> <-> TEXT)
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class TagsConverter : AttributeConverter<List<String>, String> {
    override fun convertToDatabaseColumn(attribute: List<String>?): String =
        attribute?.joinToString("|") ?: ""
    override fun convertToEntityAttribute(dbData: String?): List<String> =
        dbData?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
}
