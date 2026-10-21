package com.example.gastoapp.ui

import java.math.BigDecimal
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

object FormatoTransaccion {
    // Formateamos para mostrar, sin cambiar el BigDecimal que usa el modelo.
    // No elegimos una moneda: el requisito no especifica pesos, dólares u otra.
    fun monto(valor: BigDecimal): String = NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(valor)

    // Respetamos el idioma y la zona horaria del dispositivo.
    fun fecha(milisegundos: Long): String = DateFormat
        .getDateInstance(DateFormat.SHORT)
        .format(Date(milisegundos))
}
