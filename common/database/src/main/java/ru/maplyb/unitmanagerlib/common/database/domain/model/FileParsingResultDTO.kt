package ru.maplyb.unitmanagerlib.common.database.domain.model

data class FileParsingResultDTO(
    val headers: Map<String, List<String>>,
    val values: Map<String, List<List<String>>>
)
