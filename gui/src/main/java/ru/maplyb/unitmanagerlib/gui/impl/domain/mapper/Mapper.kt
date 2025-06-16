package ru.maplyb.unitmanagerlib.gui.impl.domain.mapper

import ru.maplyb.unitmanagerlib.common.database.domain.model.FileParsingResultDTO
import ru.maplyb.unitmanagerlib.parser.impl.FileParsingResult

fun FileParsingResultDTO.toUI(): FileParsingResult = FileParsingResult(headers = headers, values = values, tableName = tableName)