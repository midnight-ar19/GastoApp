package com.example.gastoapp.viewmodel

import com.example.gastoapp.model.TipoTransaccion
import com.example.gastoapp.model.Transaccion
import java.math.BigDecimal

// Una misma instantánea reúne datos globales y la proyección visible de las listas.
data class EstadoFinanciero(
    val transacciones: List<Transaccion>,
    val categoriaSeleccionada: String? = null
) {
    // Los totales siempre se calculan sobre la lista completa, nunca sobre el filtro.
    val totalIngresos: BigDecimal = sumar(TipoTransaccion.INGRESO)
    val totalGastos: BigDecimal = sumar(TipoTransaccion.GASTO)
    val balance: BigDecimal = totalIngresos.subtract(totalGastos)

    // Esta lista solo alimenta las pantallas de listas; Resumen usa los totales globales.
    val transaccionesVisibles: List<Transaccion> = categoriaSeleccionada?.let { categoria ->
        transacciones.filter { it.categoria == categoria }
    } ?: transacciones

    // Conservamos el orden de aparición para que el menú sea predecible y legible.
    val categoriasDisponibles: List<String> = transacciones
        .map(Transaccion::categoria)
        .filter(String::isNotBlank)
        .distinct()

    private fun sumar(tipo: TipoTransaccion): BigDecimal = transacciones
        .filter { it.tipo == tipo }
        // Partir de cero también resuelve correctamente una lista vacía.
        .fold(BigDecimal.ZERO) { total, transaccion -> total.add(transaccion.monto) }
}
