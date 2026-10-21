package com.example.gastoapp.ui.tema

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.gastoapp.R

/** Selector de una sola opción; cancelar deja intacto el modo actual. */
object DialogoTema {
    fun mostrar(
        contexto: Context,
        modoActual: ModoTema,
        alSeleccionar: (ModoTema) -> Unit
    ): AlertDialog {
        val modos = ModoTema.values().toList()
        val opciones = modos.map { modo ->
            contexto.getString(
                if (modo == ModoTema.CLARO) R.string.tema_claro else R.string.tema_oscuro
            )
        }
        val posicionActual = modos.indexOf(modoActual).coerceAtLeast(0)

        return AlertDialog.Builder(contexto)
            .setTitle(R.string.titulo_dialogo_tema)
            .setSingleChoiceItems(opciones.toTypedArray(), posicionActual) { dialogo, posicion ->
                modos.getOrNull(posicion)?.let { modo ->
                    alSeleccionar(modo)
                    dialogo.dismiss()
                }
            }
            .setNegativeButton(R.string.accion_cancelar, null)
            .create()
            .also { it.show() }
    }
}
