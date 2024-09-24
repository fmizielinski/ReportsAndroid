package pl.fmizielinski.reports.utils

import kotlin.math.pow
import kotlin.math.round

fun Float.roundToDecimalPlaces(decimalPlaces: Int): Float {
    val factor = 10.0.pow(decimalPlaces).toFloat()
    return round(this * factor) / factor
}
