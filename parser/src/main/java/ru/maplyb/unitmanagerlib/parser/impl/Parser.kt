package ru.maplyb.unitmanagerlib.parser.impl

import java.awt.AWTEventMulticaster.add
import java.io.File
import java.io.InputStream

fun parseFile(inputStream: InputStream): Map<String, List<String>> {
    val lines = buildList {
        inputStream.bufferedReader().use {
            addAll(it.readLines())
        }
    }
    check(lines.size > 2) { "Размер файла слишком маленький" }
    val headers = parseCsvHeaders(lines.take(2))
    val subHeaders = parseSubHeader(headers)
    val values = parseValues(lines.subList(2, lines.lastIndex+1))
    println("headers: $subHeaders")
    println("values: $values")
    return subHeaders
}
fun parseFile(file: File): Map<String, List<String>> {
    val lines = file.readLines()
    check(lines.size > 2) { "Размер файла слишком маленький" }
    val headers = parseCsvHeaders(lines.take(2))
    val subHeaders = parseSubHeader(headers)
    val values = parseValues(lines.subList(2, lines.lastIndex+1))
    println("headers: $subHeaders")
    println("values: $values")
    return subHeaders
}
private fun parseValues(lines: List<String>): Map<String, List<List<String>>> {
    val splittedLines = lines.map { it.split(",") }

    val sectionIndices = splittedLines.mapIndexedNotNull { index, line ->
        val nonEmpty = line.filter { it.isNotEmpty() }
        if (nonEmpty.size == 1) index to nonEmpty.first() else null
    }

    return buildMap {
        for (i in 0 until sectionIndices.lastIndex) {
            val (startIndex, key) = sectionIndices[i]
            val (endIndex, _) = sectionIndices[i + 1]
            put(key, splittedLines.subList(startIndex + 1, endIndex))
        }

        val (lastStartIndex, lastKey) = sectionIndices.last()
        put(lastKey, splittedLines.subList(lastStartIndex + 1, splittedLines.size))
    }
}
private fun parseSubHeader(headers: List<String>): Map<String, List<String>> {
    val headersWithSubHeaders = headers
        .groupBy { it.substringBefore('_') }
        .mapValues { (_, group) ->
            val subHeaders = group.mapNotNull {
                it
                    .substringAfter('_', "")
                    .takeIf { sub -> sub.isNotEmpty() }
            }
            subHeaders
        }
    return headersWithSubHeaders
}

private fun parseCsvHeaders(lines: List<String>): List<String> {

    val topHeaders = lines[0].split(",").map { it.trim().removeSurrounding("\"") }
    val subHeaders = lines[1].split(",").map { it.trim().removeSurrounding("\"") }

    val result = mutableListOf<String>()
    var lastTop = ""

    for (i in topHeaders.indices) {
        val top = topHeaders.getOrElse(i) { "" }.ifBlank { lastTop }
        val sub = subHeaders.getOrElse(i) { "" }

        if (top.isNotBlank()) lastTop = top

        val header = when {
            sub.isNotBlank() && top.isNotBlank() -> "${top}_${sub}"
            sub.isNotBlank() && top.isBlank() -> "${lastTop}_${sub}" // наследуем top
            sub.isBlank() && top.isNotBlank() -> top
            else -> "column_$i" // резервное имя
        }

        result.add(header)
    }

    return result
}

data class RowInfo(
    val npp: String?,
    val number: Int?,
    val pozivnoi: String?,
    val zheton: String?,
    val dolzhnost: String?,
    val gruppa: String?,
    val vooruzhenie: List<Vooruzhenie>,
    val sredstvoSviazy: List<String?>,
    val gruppaKrovi: String?,
    val pozicia: String?
)

data class Vooruzhenie(
    val info: Pair<String?, String?>
)