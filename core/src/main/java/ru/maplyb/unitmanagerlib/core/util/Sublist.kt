package ru.maplyb.unitmanagerlib.core.util

fun <T> List<T>.subListExclusiveToInclusive(fromExclusive: Int, toInclusive: Int): List<T> {
    if (fromExclusive >= size || toInclusive < 0 || fromExclusive >= toInclusive) return emptyList()
    return subList(fromExclusive + 1, minOf(toInclusive + 1, size))
}