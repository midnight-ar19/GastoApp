package com.example.gastoapp.ui.tema

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Guarda únicamente el modo visual elegido y lo entrega a AppCompat antes de inflar la pantalla.
 */
object TemaPreferencias {
    private const val NOMBRE_PREFERENCIAS = "preferencias_presentacion"
    private const val CLAVE_MODO = "modo_tema"

    /** Lee la elección; los valores ausentes o desconocidos se interpretan como Claro. */
    fun leer(contexto: Context): ModoTema {
        val valor = contexto
            .getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE)
            .getString(CLAVE_MODO, null)
        return ModoTema.desdeValor(valor)
    }

    /** Se invoca antes de super.onCreate para que la primera vista nazca con el modo correcto. */
    fun aplicarGuardado(contexto: Context) {
        aplicar(leer(contexto))
    }

    /** Persiste y aplica el modo de una sola vez para que la selección sea inmediata. */
    fun guardarYAplicar(contexto: Context, modo: ModoTema) {
        contexto
            .getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE)
            .edit()
            .putString(CLAVE_MODO, modo.valorPersistido)
            .apply()
        aplicar(modo)
    }

    private fun aplicar(modo: ModoTema) {
        // Evitar la llamada repetida evita recreaciones innecesarias durante onCreate.
        if (AppCompatDelegate.getDefaultNightMode() != modo.modoNoche) {
            AppCompatDelegate.setDefaultNightMode(modo.modoNoche)
        }
    }
}
