package com.example.gastoapp.viewmodel

import com.example.gastoapp.model.TipoTransaccion
import com.example.gastoapp.model.Transaccion
import java.math.BigDecimal
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedViewModelTest {
    @Test
    fun calculaTotalesSeparandoIngresosYGastos() {
        val estado = EstadoFinanciero(
            listOf(
                transaccion(1, "0.10", TipoTransaccion.INGRESO),
                transaccion(2, "0.20", TipoTransaccion.INGRESO),
                transaccion(3, "0.05", TipoTransaccion.GASTO)
            )
        )
        // Comparamos valores decimales, sin exigir una escala de presentación.
        assertDinero("0.30", estado.totalIngresos)
        assertDinero("0.05", estado.totalGastos)
        assertDinero("0.25", estado.balance)
    }

    @Test
    fun unaListaVaciaTieneTotalesEnCero() {
        val estado = EstadoFinanciero(emptyList())
        assertDinero("0", estado.totalIngresos)
        assertDinero("0", estado.totalGastos)
        assertDinero("0", estado.balance)
    }

    @Test
    fun permiteRepresentarUnBalanceNegativo() {
        val estado = EstadoFinanciero(listOf(transaccion(1, "25.50", TipoTransaccion.GASTO)))
        assertDinero("0", estado.totalIngresos)
        assertDinero("25.50", estado.totalGastos)
        assertDinero("-25.50", estado.balance)
    }

    @Test
    fun precargaEjemplosDeAmbosTiposYDeAmbosPeriodos() {
        val estado = SharedViewModel().estado.value
        assertEquals(5, estado.transacciones.size)
        assertEquals(5, estado.transacciones.map { it.id }.distinct().size)
        assertTrue(estado.transacciones.any { it.tipo == TipoTransaccion.INGRESO })
        assertTrue(estado.transacciones.any { it.tipo == TipoTransaccion.GASTO })
        assertTrue(estado.transacciones.all { it.descripcion.isNotBlank() && it.categoria.isNotBlank() })
        val inicioMes = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val gastos = estado.transacciones.filter { it.tipo == TipoTransaccion.GASTO }
        assertTrue(gastos.any { it.fecha >= inicioMes })
        assertTrue(gastos.any { it.fecha < inicioMes })
        assertDinero("1250.00", estado.totalIngresos)
        assertDinero("290.50", estado.totalGastos)
        assertDinero("959.50", estado.balance)
    }

    private fun transaccion(id: Long, monto: String, tipo: TipoTransaccion) = Transaccion(
        id = id,
        descripcion = "Ejemplo",
        monto = BigDecimal(monto),
        categoria = "Otros",
        fecha = 0L,
        tipo = tipo
    )

    private fun assertDinero(esperado: String, actual: BigDecimal) {
        assertEquals("Monto esperado: $esperado; recibido: $actual", 0, BigDecimal(esperado).compareTo(actual))
    }
}
