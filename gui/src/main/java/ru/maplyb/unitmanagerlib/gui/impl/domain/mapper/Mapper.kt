package ru.maplyb.unitmanagerlib.gui.impl.domain.mapper

import ru.maplyb.unitmanagerlib.common.database.domain.model.FileParsingResultDTO
import ru.maplyb.unitmanagerlib.common.database.domain.model.PositionDTO
import ru.maplyb.unitmanagerlib.gui.api.model.Position
import ru.maplyb.unitmanagerlib.gui.impl.domain.PositionUI
import ru.maplyb.unitmanagerlib.parser.impl.FileParsingResult

internal fun FileParsingResultDTO.toUI(): FileParsingResult = FileParsingResult(
    headers = headers,
    values = values,
    valueTypes = valueTypes,
    tableName = tableName
)

internal fun Position.toDTO(): PositionDTO = PositionDTO(x = x, y = y, name = name)
internal fun PositionDTO.toUI(): PositionUI = PositionUI(id = id, x = x, y = y, name = name)

