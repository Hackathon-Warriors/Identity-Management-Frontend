package com.thales.idverification.utils

fun <E> MutableList<E>.clearAndAddAll(list: List<E>) {
    clear()
    addAll(list)
}
