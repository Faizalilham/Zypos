package dev.faizal.core.common.utils

import java.text.NumberFormat
import java.util.Locale

fun Float.toPercentageString(decimals: Int = 1): String {
    return "%.${decimals}f%%".format(this * 100)
}

fun Double.toPercentageString(decimals: Int = 1): String {
    return "%.${decimals}f%%".format(this * 100)
}

fun Double.toDecimalString(decimals: Int = 2): String {
    return "%.${decimals}f".format(Locale.ROOT, this)
}

fun Float.toDecimalString(decimals: Int = 2): String {
    return "%.${decimals}f".format(Locale.ROOT, this)
}

fun Double.toRupiahFormatDecimal(decimals: Int = 0): String {
    return "Rp ${"%.${decimals}f".format(Locale.ROOT, this)}"
}

fun Double.toRupiahString(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(this)
}

fun Double.toCurrencyString(prefix: String = "Rp "): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return "$prefix${formatter.format(this)}"
}

// For Int/Long
fun Int.toRupiahString(): String = this.toDouble().toRupiahString()
fun Long.toRupiahString(): String = this.toDouble().toRupiahString()