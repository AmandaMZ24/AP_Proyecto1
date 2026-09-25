package com.example.controldeflotillastransandina.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es"))
private val moneyFormatter = NumberFormat.getNumberInstance(Locale("es")).apply {
    maximumFractionDigits = 0
}

fun Long.epochDayToLocalDate(): LocalDate = LocalDate.ofEpochDay(this)

fun LocalDate.toEpochDayLong(): Long = toEpochDay()

fun LocalDate.formatDate(): String = format(dateFormatter)

fun Long.formatEpochDay(): String = LocalDate.ofEpochDay(this).format(dateFormatter)

fun Long.formatEpochDayShort(): String =
    LocalDate.ofEpochDay(this).format(DateTimeFormatter.ofPattern("dd MMM", Locale("es")))

fun Long.epochMillisToLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun Double.formatMoney(): String = "₡${DecimalFormat("#,##0").format(this)}"

fun Double.formatKm(): String = "%,d".format(toLong()).replace(',', ' ')

fun String.capitalizeEs(): String = replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }