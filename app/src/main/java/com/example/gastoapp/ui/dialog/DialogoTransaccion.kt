package com.example.gastoapp.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import com.example.gastoapp.R
import com.example.gastoapp.databinding.DialogTransaccionBinding
import com.example.gastoapp.model.TipoTransaccion
import com.example.gastoapp.viewmodel.ErroresTransaccion
import com.example.gastoapp.viewmodel.SharedViewModel

// Un único helper reutiliza el formulario para altas de ingresos y de gastos.
class DialogoTransaccion(
    private val contexto: Context,
    private val viewModel: SharedViewModel,
    private val tipo: TipoTransaccion
) {
    private var dialogo: AlertDialog? = null

    fun mostrar(): AlertDialog {
        dismiss()
        val binding = DialogTransaccionBinding.inflate(LayoutInflater.from(contexto))
        val titulo = if (tipo == TipoTransaccion.INGRESO) {
            R.string.titulo_dialogo_ingreso
        } else {
            R.string.titulo_dialogo_gasto
        }
        val nuevoDialogo = AlertDialog.Builder(contexto)
            .setTitle(titulo)
            .setView(binding.root)
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_guardar, null)
            .create()

        nuevoDialogo.setOnShowListener {
            // El listener se reemplaza para impedir que un error cierre el formulario.
            nuevoDialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val resultado = viewModel.agregarTransaccion(
                    monto = binding.campoMonto.text?.toString().orEmpty(),
                    descripcion = binding.campoDescripcion.text?.toString().orEmpty(),
                    categoria = binding.campoCategoria.text?.toString().orEmpty(),
                    tipo = tipo
                )
                if (resultado.esExito) {
                    nuevoDialogo.dismiss()
                } else {
                    mostrarErrores(binding, resultado.errores)
                }
            }
        }
        limpiarErrorAlEscribir(binding)
        nuevoDialogo.setOnDismissListener {
            if (dialogo === nuevoDialogo) {
                dialogo = null
            }
        }
        dialogo = nuevoDialogo
        nuevoDialogo.show()
        return nuevoDialogo
    }

    fun dismiss() {
        dialogo?.dismiss()
        dialogo = null
    }

    private fun mostrarErrores(
        binding: DialogTransaccionBinding,
        errores: ErroresTransaccion
    ) {
        binding.campoMontoLayout.error = errores.monto
        binding.campoDescripcionLayout.error = errores.descripcion
        binding.campoCategoriaLayout.error = errores.categoria
        when {
            errores.monto != null -> binding.campoMonto.requestFocus()
            errores.descripcion != null -> binding.campoDescripcion.requestFocus()
            errores.categoria != null -> binding.campoCategoria.requestFocus()
        }
    }

    private fun limpiarErrorAlEscribir(binding: DialogTransaccionBinding) {
        binding.campoMonto.doAfterTextChanged { binding.campoMontoLayout.error = null }
        binding.campoDescripcion.doAfterTextChanged {
            binding.campoDescripcionLayout.error = null
        }
        binding.campoCategoria.doAfterTextChanged { binding.campoCategoriaLayout.error = null }
    }
}
