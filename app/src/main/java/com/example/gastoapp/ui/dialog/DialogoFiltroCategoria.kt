package com.example.gastoapp.ui.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.gastoapp.R

// Crea el selector reutilizable sin añadir otro Fragment al grafo de navegación.
object DialogoFiltroCategoria {
    fun mostrar(
        contexto: Context,
        categorias: List<String>,
        categoriaSeleccionada: String?,
        alSeleccionar: (String?) -> Unit
    ): AlertDialog {
        val opciones = buildList {
            add(contexto.getString(R.string.filtro_todas_categorias))
            addAll(categorias)
        }
        val posicionSeleccionada = categoriaSeleccionada
            ?.let { categoria -> categorias.indexOf(categoria).takeIf { it >= 0 }?.plus(1) }
            ?: 0

        return AlertDialog.Builder(contexto)
            .setTitle(R.string.titulo_dialogo_filtro)
            .setSingleChoiceItems(opciones.toTypedArray(), posicionSeleccionada) { dialogo, posicion ->
                alSeleccionar(categorias.getOrNull(posicion - 1))
                dialogo.dismiss()
            }
            .setNegativeButton(R.string.accion_cancelar, null)
            .create()
            .also { it.show() }
    }
}
