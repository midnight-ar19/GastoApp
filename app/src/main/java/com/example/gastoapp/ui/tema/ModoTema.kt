package com.example.gastoapp.ui.tema

import androidx.appcompat.app.AppCompatDelegate

/**
 * Modos de presentación que la aplicación puede recordar.
 *
 * El valor persistido es deliberadamente estable y pequeño: no representa datos financieros ni
 * una configuración del sistema, solo la elección visual de esta aplicación.
 */
enum class ModoTema(
    val valorPersistido: String,
    val modoNoche: Int
) {
    CLARO("claro", AppCompatDelegate.MODE_NIGHT_NO),
    OSCURO("oscuro", AppCompatDelegate.MODE_NIGHT_YES);

    companion object {
        /** Un valor ausente o antiguo vuelve al modo claro, que es el valor predeterminado. */
        fun desdeValor(valorPersistido: String?): ModoTema =
            values().firstOrNull { it.valorPersistido == valorPersistido } ?: CLARO
    }
}
