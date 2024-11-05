package com.thales.idverification.modules.customviews.model

data class TableItemView(
    val value: String?,
    val padding: Int,
    val rowId: Int,
    val columnIndex: Int,
    val isHeader: Boolean = false
)
