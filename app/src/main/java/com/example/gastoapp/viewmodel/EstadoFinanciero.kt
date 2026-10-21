package com.example.gastoapp.viewmodel

import com.example.gastoapp.model.TipoTransaccion
import com.example.gastoapp.model.Transaccion
import java.math.BigDecimal

// Una misma instantánea reúne la lista y sus totales para no mostrar datos desfasados.
data class EstadoFinanciero(val transacciones: List<Transaccion>) {
    val totalIngresos: BigDecimal = sumar(TipoTransaccion.INGRESO)
    val totalGastos: BigDecimal = sumar(TipoTransaccion.GASTO)
    val balance: BigDecimal = totalIngresos.subtract(totalGastos)

    private fun sumar(tipo: TipoTransaccion): BigDecimal = transacciones
        .filter { it.tipo == tipo }
        // Partir de cero también resuelve correctamente una lista vacía.
        .fold(BigDecimal.ZERO) { total, transaccion -> total.add(transaccion.monto) }
}
