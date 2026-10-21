package com.example.gastoapp.ui.gastos

import com.example.gastoapp.model.TipoTransaccion
import com.example.gastoapp.model.Transaccion
import java.util.Calendar
import java.util.TimeZone

enum class PeriodoGastos {
    ESTE_MES,
    ANTERIORES
}

/**
 * Selecciona los gastos de una pestaña usando límites de mes en la zona local.
 * La referencia explícita permite probar los cambios de mes sin depender del reloj real.
 */
fun seleccionarGastos(
    transacciones: List<Transaccion>,
    periodo: PeriodoGastos,
    referencia: Long,
    zonaHoraria: TimeZone = TimeZone.getDefault()
): List<Transaccion> {
    val inicioMesActual = inicioDelMes(referencia, zonaHoraria)
    val inicioMesSiguiente = (inicioMesActual.clone() as Calendar).apply {
        add(Calendar.MONTH, 1)
    }.timeInMillis

    return transacciones.filter { transaccion ->
        if (transaccion.tipo != TipoTransaccion.GASTO) {
            return@filter false
        }
        when (periodo) {
            PeriodoGastos.ESTE_MES ->
                transaccion.fecha >= inicioMesActual.timeInMillis &&
                    transaccion.fecha < inicioMesSiguiente
            PeriodoGastos.ANTERIORES -> transaccion.fecha < inicioMesActual.timeInMillis
        }
    }
}

private fun inicioDelMes(referencia: Long, zonaHoraria: TimeZone): Calendar =
    Calendar.getInstance(zonaHoraria).apply {
        timeInMillis = referencia
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
