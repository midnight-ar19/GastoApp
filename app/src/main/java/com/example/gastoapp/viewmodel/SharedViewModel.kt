package com.example.gastoapp.viewmodel

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.example.gastoapp.model.DatosEjemplo
import com.example.gastoapp.model.TipoTransaccion
import com.example.gastoapp.model.Transaccion
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Los errores se separan por campo para que el diálogo pueda explicar cada corrección.
data class ErroresTransaccion(
    val monto: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null
) {
    val tieneErrores: Boolean
        get() = monto != null || descripcion != null || categoria != null
}

// El resultado público permite conservar el diálogo abierto cuando la validación falla.
data class ResultadoAltaTransaccion(
    val transaccion: Transaccion? = null,
    val errores: ErroresTransaccion = ErroresTransaccion()
) {
    val esExito: Boolean
        get() = transaccion != null && !errores.tieneErrores
}

class SharedViewModel : ViewModel() {
    // La precarga ocurre una sola vez por instancia del ViewModel, no por Fragment.
    private val _estado = MutableStateFlow(
        EstadoFinanciero(DatosEjemplo.crearTransacciones())
    )

    // Las pantallas pueden observar el estado, pero no asignarle nuevos valores.
    val estado: StateFlow<EstadoFinanciero> = _estado.asStateFlow()

    /**
     * Valida y agrega una transacción de forma síncrona.
     * La fecha por defecto se toma al guardar, no al crear el diálogo.
     */
    // La UI invoca las mutaciones en el hilo principal para publicar una instantánea síncrona.
    @MainThread
    fun agregarTransaccion(
        monto: String,
        descripcion: String,
        categoria: String,
        tipo: TipoTransaccion,
        fecha: Long = System.currentTimeMillis()
    ): ResultadoAltaTransaccion {
        val montoLimpio = monto.trim()
        val descripcionLimpia = descripcion.trim()
        val categoriaLimpia = categoria.trim()
        val errorMonto = validarMonto(montoLimpio)
        val errores = ErroresTransaccion(
            monto = errorMonto,
            descripcion = if (descripcionLimpia.isBlank()) {
                "La descripción es obligatoria."
            } else {
                null
            },
            categoria = if (categoriaLimpia.isBlank()) {
                "La categoría es obligatoria."
            } else {
                null
            }
        )

        // Una entrada inválida termina antes de tocar la lista, los totales o el filtro.
        if (errores.tieneErrores) {
            return ResultadoAltaTransaccion(errores = errores)
        }

        val montoExacto = normalizarMonto(montoLimpio)
        val estadoActual = _estado.value
        val transaccion = Transaccion(
            id = siguienteId(estadoActual.transacciones),
            descripcion = descripcionLimpia,
            monto = montoExacto,
            categoria = categoriaLimpia,
            fecha = fecha,
            tipo = tipo
        )
        // Una sola asignación publica lista, totales derivados y filtro coherentes.
        _estado.value = EstadoFinanciero(
            transacciones = estadoActual.transacciones + transaccion,
            categoriaSeleccionada = estadoActual.categoriaSeleccionada
        )
        return ResultadoAltaTransaccion(transaccion = transaccion)
    }

    /** Selecciona una categoría para las listas; null representa Todas las categorías. */
    // La UI invoca las mutaciones en el hilo principal para publicar una instantánea síncrona.
    @MainThread
    fun seleccionarCategoria(categoria: String?) {
        val categoriaLimpia = categoria?.trim()?.takeIf { it.isNotEmpty() }
        val estadoActual = _estado.value
        if (estadoActual.categoriaSeleccionada == categoriaLimpia) {
            return
        }
        _estado.value = EstadoFinanciero(
            transacciones = estadoActual.transacciones,
            categoriaSeleccionada = categoriaLimpia
        )
    }

    /** Limpia el filtro sin cambiar las transacciones ni sus totales. */
    @MainThread
    fun limpiarFiltroCategoria() = seleccionarCategoria(null)

    private fun validarMonto(monto: String): String? {
        if (monto.isBlank()) {
            return "El monto es obligatorio."
        }
        if (!PATRON_MONTO.matches(monto)) {
            return "Usa un número positivo con hasta dos decimales y sin separadores de miles."
        }
        if (normalizarMonto(monto).compareTo(BigDecimal.ZERO) <= 0) {
            return "El monto debe ser mayor que cero."
        }
        return null
    }

    private fun normalizarMonto(monto: String): BigDecimal {
        val normalizado = monto.replace(',', '.')
        return BigDecimal(if (normalizado.startsWith('.')) "0$normalizado" else normalizado)
    }

    private fun siguienteId(transacciones: List<Transaccion>): Long {
        val ids = transacciones.mapTo(mutableSetOf()) { it.id }
        var candidato = 1L
        while (ids.contains(candidato)) {
            check(candidato != Long.MAX_VALUE) { "No hay más identificadores disponibles." }
            candidato++
        }
        return candidato
    }

    private companion object {
        // Solo se permite un separador decimal y una o dos cifras fraccionarias.
        val PATRON_MONTO = Regex("^(?:[0-9]+(?:[.,][0-9]{1,2})?|[.,][0-9]{1,2})$")
    }
}
