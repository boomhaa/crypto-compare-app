package com.example.helpers

import java.util.Locale

fun Double.toPriceString(): String = String.format(Locale.US, "%.6f", this)
