package dev.faizal.core.common.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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


fun Double.toCurrencyString(prefix: String = "Rp "): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return "$prefix${formatter.format(this)}"
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}