package com.thales.idverification.utils

import java.math.RoundingMode

fun Double.round(decimals: Int): Double =
    this.toBigDecimal().setScale(decimals, RoundingMode.DOWN).toDouble()
