package com.example.gastoapp.ui.gastos

import com.example.gastoapp.model.TipoTransaccion
import com.example.gastoapp.model.Transaccion
import java.math.BigDecimal
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

class GastosPeriodosTest {
    @Test
    fun esteMesIncluyeLosLimitesCorrectosYExcluyeIngresosYFuturo() {
        val referencia = fecha(2025, Calendar.JANUARY, 15, 12)
        val transacciones = listOf(
            gasto(1, fecha(2024, Calendar.DECEMBER, 31, 23, 59, 59, 999)),
            gasto(2, fecha(2025, Calendar.JANUARY, 1)),
            gasto(3, fecha(2025, Calendar.JANUARY, 31, 23, 59, 59, 999)),
            gasto(4, fecha(2025, Calendar.FEBRUARY, 1)),
            Transaccion(
                5,
                "Ingreso del mes",
                BigDecimal("10.00"),
                "Trabajo",
                fecha(2025, Calendar.JANUARY, 1),
                TipoTransaccion.INGRESO
            )
        )

        val resultado = seleccionarGastos(transacciones, PeriodoGastos.ESTE_MES, referencia)

        assertEquals(listOf(2L, 3L), resultado.map(Transaccion::id))
    }

    @Test
    fun anterioresIncluyeSoloGastosAnterioresAlInicioDelMes() {
        val referencia = fecha(2025, Calendar.JANUARY, 15, 12)
        val transacciones = listOf(
            gasto(1, fecha(2024, Calendar.DECEMBER, 31, 23, 59, 59, 999)),
            gasto(2, fecha(2025, Calendar.JANUARY, 1)),
            gasto(3, fecha(2025, Calendar.FEBRUARY, 1)),
            Transaccion(
                4,
                "Ingreso anterior",
                BigDecimal("10.00"),
                "Trabajo",
                fecha(2024, Calendar.DECEMBER, 31, 23, 59, 59, 999),
                TipoTransaccion.INGRESO
            )
        )

        val resultado = seleccionarGastos(transacciones, PeriodoGastos.ANTERIORES, referencia)

        assertEquals(listOf(1L), resultado.map(Transaccion::id))
    }

    @Test
    fun diciembreCalculaEneroComoSiguienteMesAlCambiarDeAnio() {
        val referencia = fecha(2024, Calendar.DECEMBER, 15, 12)
        val transacciones = listOf(
            gasto(1, fecha(2024, Calendar.NOVEMBER, 30, 23, 59, 59, 999)),
            gasto(2, fecha(2024, Calendar.DECEMBER, 1)),
            gasto(3, fecha(2024, Calendar.DECEMBER, 31, 23, 59, 59, 999)),
            gasto(4, fecha(2025, Calendar.JANUARY, 1))
        )

        val resultado = seleccionarGastos(transacciones, PeriodoGastos.ESTE_MES, referencia)

        assertEquals(listOf(2L, 3L), resultado.map(Transaccion::id))
    }

    private fun gasto(id: Long, fecha: Long): Transaccion = Transaccion(
        id = id,
        descripcion = "Gasto $id",
        monto = BigDecimal("1.00"),
        categoria = "Prueba",
        fecha = fecha,
        tipo = TipoTransaccion.GASTO
    )

    private fun fecha(
        anio: Int,
        mes: Int,
        dia: Int,
        hora: Int = 0,
        minuto: Int = 0,
        segundo: Int = 0,
        milisegundo: Int = 0
    ): Long = Calendar.getInstance().apply {
        clear()
        set(anio, mes, dia, hora, minuto, segundo)
        set(Calendar.MILLISECOND, milisegundo)
    }.timeInMillis
}
