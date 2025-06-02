package ru.maplyb.unitmanagerlib.core.util


fun List<List<String>>.copyMap(
    newValue: String,
    columnIndex: Int,
    rowIndex: Int
): List<List<String>> {
    val mutableList = this[columnIndex].toMutableList()
    mutableList[rowIndex] = newValue
    val mutableValuesList = this.toMutableList()
    mutableValuesList[columnIndex] = mutableList
    return mutableValuesList
}

fun List<List<String>>.getValuesByIndex(index: Int): List<String> {
    return map { it.getOrNull(index) ?: "" }
}