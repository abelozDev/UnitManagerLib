package ru.maplyb.unitmanagerlib.parser.impl


fun convertToCsv(
    headersData: Map<String, List<String>>,
    values: Map<String, List<List<String>>>,
): List<String> {
    val size = headersData.entries.sumOf {
        if (it.value.isNotEmpty()) it.value.size else 1
    }
    val headers = convertHeaders(headersData)
    val convertedValues = convertValues(values, size)
    return headers + convertedValues
}
private fun convertValues(values: Map<String, List<List<String>>>, size: Int): List<String> {
    val valueList = mutableListOf<String>()
    values.forEach { currentValue ->
        val type = StringBuilder()
        for (i in 0..size-1) {
            if (i == 0) type.append(currentValue.key) else type.append(",")
        }
        valueList.add(type.toString())
        currentValue.value.forEach {
            val value = StringBuilder()
            for (i in 0..size-1) {
                value.append(it[i])
                if (i < size-1) value.append(",")
            }
            valueList.add(value.toString())
        }

    }
    return valueList
}
private fun convertHeaders(headersData: Map<String, List<String>>): List<String> {
    val line1 = StringBuilder()
    val line2 = StringBuilder()
    val entries = headersData.entries.toList()
    entries.forEachIndexed { index, (header, subheaders) ->
        line1.apply {
            append(header)
            if (subheaders.isNotEmpty()) {
                repeat(subheaders.size) {
                    append(",")
                }
            } else {
                if (index != entries.lastIndex) {
                    append(",")
                }
            }
        }
        line2.apply {
            if (subheaders.isEmpty()) {
                if (index != entries.lastIndex) {
                    append(",")
                }
            } else {
                subheaders.forEach {
                    append(it)
                    append(",")
                }
            }
        }
    }
    return listOf(line1.toString(), line2.toString())
}