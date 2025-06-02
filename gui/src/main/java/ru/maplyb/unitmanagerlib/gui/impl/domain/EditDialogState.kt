package ru.maplyb.unitmanagerlib.gui.impl.domain

internal data class EditDialogState(
    val name: String = "",
    val visibility: Boolean = false,
    val confirm: (String) -> Unit,
    val dismiss: () -> Unit
) {
    companion object {
        val default = EditDialogState(
            "", false, {}, {}
        )
    }
}