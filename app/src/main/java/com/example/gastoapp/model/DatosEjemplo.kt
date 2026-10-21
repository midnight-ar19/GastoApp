package com.example.gastoapp.model

import java.math.BigDecimal
import java.util.Calendar

object DatosEjemplo {
    fun crearTransacciones(): List<Transaccion> {
        // Fijamos el día antes de cambiar de mes para evitar desbordes del día 31.
        val esteMes = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val mesAnterior = (esteMes.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        // Siempre hay ejemplos de ambos períodos, sin importar cuándo se abre la app.
        return listOf(
            Transaccion(1L, "Sueldo", BigDecimal("1000.00"), "Trabajo", esteMes.timeInMillis, TipoTransaccion.INGRESO),
            Transaccion(2L, "Clase particular", BigDecimal("250.00"), "Educación", esteMes.timeInMillis, TipoTransaccion.INGRESO),
            Transaccion(3L, "Supermercado", BigDecimal("150.50"), "Alimentación", esteMes.timeInMillis, TipoTransaccion.GASTO),
            Transaccion(4L, "Colectivo", BigDecimal("40.00"), "Transporte", esteMes.timeInMillis, TipoTransaccion.GASTO),
            Transaccion(5L, "Servicio de internet", BigDecimal("100.00"), "Servicios", mesAnterior.timeInMillis, TipoTransaccion.GASTO)
        )
    }
}
