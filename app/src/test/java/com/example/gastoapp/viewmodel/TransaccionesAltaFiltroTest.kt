package com.example.gastoapp.viewmodel

import com.example.gastoapp.model.TipoTransaccion
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransaccionesAltaFiltroTest {
    @Test
    fun agregaIngresoYGastoActualizaListaTotalesBalanceYUsaIdsUnicos() {
        val viewModel = SharedViewModel()

        val ingreso = viewModel.agregarTransaccion(
            monto = "100,25",
            descripcion = "Trabajo extra",
            categoria = "Trabajo",
            tipo = TipoTransaccion.INGRESO,
            fecha = 1_700_000_000_000L
        )
        val gasto = viewModel.agregarTransaccion(
            monto = "40.10",
            descripcion = "Libro",
            categoria = "Educación",
            tipo = TipoTransaccion.GASTO,
            fecha = 1_700_000_000_001L
        )

        assertTrue(ingreso.esExito)
        assertTrue(gasto.esExito)
        assertNotNull(ingreso.transaccion)
        assertNotNull(gasto.transaccion)
        assertEquals(6L, ingreso.transaccion?.id)
        assertEquals(7L, gasto.transaccion?.id)
        assertEquals(
            viewModel.estado.value.transacciones.size,
            viewModel.estado.value.transacciones.map { it.id }.distinct().size
        )
        assertDinero("1350.25", viewModel.estado.value.totalIngresos)
        assertDinero("330.60", viewModel.estado.value.totalGastos)
        assertDinero("1019.65", viewModel.estado.value.balance)
        assertTrue(viewModel.estado.value.transacciones.any { it.descripcion == "Trabajo extra" })
        assertTrue(viewModel.estado.value.transacciones.any { it.descripcion == "Libro" })
    }

    @Test
    fun aceptaComaOPuntoYConservaElDecimalExacto() {
        val viewModel = SharedViewModel()

        val conComa = viewModel.agregarTransaccion(
            "12,30",
            "Coma",
            "Pruebas",
            TipoTransaccion.INGRESO
        )
        val conPunto = viewModel.agregarTransaccion(
            "7.05",
            "Punto",
            "Pruebas",
            TipoTransaccion.INGRESO
        )

        assertTrue(conComa.esExito)
        assertTrue(conPunto.esExito)
        assertEquals(0, BigDecimal("12.30").compareTo(conComa.transaccion?.monto))
        assertEquals(0, BigDecimal("7.05").compareTo(conPunto.transaccion?.monto))
    }

    @Test
    fun rechazaEntradasInvalidasSinCambiarEstadoFiltroNiConsumirId() {
        val viewModel = SharedViewModel()
        viewModel.seleccionarCategoria("Trabajo")
        val estadoInicial = viewModel.estado.value
        val idsIniciales = estadoInicial.transacciones.map { it.id }

        val rechazos = listOf(
            Triple("", "Monto vacío", "monto"),
            Triple("abc", "Monto mal formado", "monto"),
            Triple("0", "Monto cero", "monto"),
            Triple("-1", "Monto negativo", "monto"),
            Triple("1,234", "Más de dos decimales", "monto"),
            Triple("1.000,00", "Agrupación no permitida", "monto"),
            Triple("1", "   ", "descripcion"),
            Triple("1", "Descripción válida", "categoria")
        )

        rechazos.forEach { (monto, descripcion, campo) ->
            val resultado = viewModel.agregarTransaccion(
                monto = monto,
                descripcion = descripcion,
                categoria = if (campo == "categoria") "   " else "Pruebas",
                tipo = TipoTransaccion.INGRESO
            )

            assertFalse("Debe rechazar $campo", resultado.esExito)
            when (campo) {
                "monto" -> assertNotNull(resultado.errores.monto)
                "descripcion" -> assertNotNull(resultado.errores.descripcion)
                "categoria" -> assertNotNull(resultado.errores.categoria)
            }
            assertEquals(idsIniciales, viewModel.estado.value.transacciones.map { it.id })
            assertEquals(estadoInicial.transacciones, viewModel.estado.value.transacciones)
            assertEquals(estadoInicial.categoriaSeleccionada, viewModel.estado.value.categoriaSeleccionada)
            assertEquals(estadoInicial.totalIngresos, viewModel.estado.value.totalIngresos)
            assertEquals(estadoInicial.totalGastos, viewModel.estado.value.totalGastos)
            assertEquals(estadoInicial.balance, viewModel.estado.value.balance)
        }

        val valido = viewModel.agregarTransaccion(
            "1.00",
            "Después de errores",
            "Trabajo",
            TipoTransaccion.INGRESO
        )
        assertTrue(valido.esExito)
        assertEquals(6L, valido.transaccion?.id)
    }

    @Test
    fun filtraSoloLasListasPeroMantieneTotalesGlobalesYPermiteLimpiar() {
        val viewModel = SharedViewModel()
        val ingresosGlobales = viewModel.estado.value.totalIngresos
        val gastosGlobales = viewModel.estado.value.totalGastos
        val balanceGlobal = viewModel.estado.value.balance

        viewModel.seleccionarCategoria("Trabajo")
        val filtrado = viewModel.estado.value

        assertEquals("Trabajo", filtrado.categoriaSeleccionada)
        assertTrue(filtrado.transaccionesVisibles.isNotEmpty())
        assertTrue(filtrado.transaccionesVisibles.all { it.categoria == "Trabajo" })
        assertEquals(1, filtrado.transaccionesVisibles.size)
        assertEquals(ingresosGlobales, filtrado.totalIngresos)
        assertEquals(gastosGlobales, filtrado.totalGastos)
        assertEquals(balanceGlobal, filtrado.balance)

        viewModel.seleccionarCategoria(null)
        val sinFiltro = viewModel.estado.value
        assertEquals(null, sinFiltro.categoriaSeleccionada)
        assertEquals(sinFiltro.transacciones, sinFiltro.transaccionesVisibles)
        assertEquals(ingresosGlobales, sinFiltro.totalIngresos)
        assertEquals(gastosGlobales, sinFiltro.totalGastos)
        assertEquals(balanceGlobal, sinFiltro.balance)
    }

    @Test
    fun unaCategoriaNuevaSeVuelveDisponibleParaElFiltro() {
        val viewModel = SharedViewModel()
        val nuevaCategoria = "Mascotas"

        val resultado = viewModel.agregarTransaccion(
            "20.00",
            "Veterinario",
            nuevaCategoria,
            TipoTransaccion.GASTO
        )

        assertTrue(resultado.esExito)
        assertTrue(viewModel.estado.value.categoriasDisponibles.contains(nuevaCategoria))
        viewModel.seleccionarCategoria(nuevaCategoria)
        assertEquals(
            listOf("Veterinario"),
            viewModel.estado.value.transaccionesVisibles.map { it.descripcion }
        )
    }

    @Test
    fun losTextosSeGuardanRecortadosYElFiltroActivoOcultaUnAltaQueNoCoincide() {
        val viewModel = SharedViewModel()
        viewModel.seleccionarCategoria("Trabajo")

        val resultado = viewModel.agregarTransaccion(
            "9.99",
            "  Compra  ",
            "  Hogar  ",
            TipoTransaccion.GASTO
        )

        assertTrue(resultado.esExito)
        assertEquals("Compra", resultado.transaccion?.descripcion)
        assertEquals("Hogar", resultado.transaccion?.categoria)
        assertEquals("Trabajo", viewModel.estado.value.categoriaSeleccionada)
        assertTrue(viewModel.estado.value.transacciones.any { it.descripcion == "Compra" })
        assertTrue(viewModel.estado.value.transaccionesVisibles.none { it.descripcion == "Compra" })
    }

    private fun assertDinero(esperado: String, actual: BigDecimal) {
        assertEquals("Monto esperado: $esperado; recibido: $actual", 0, BigDecimal(esperado).compareTo(actual))
    }
}
