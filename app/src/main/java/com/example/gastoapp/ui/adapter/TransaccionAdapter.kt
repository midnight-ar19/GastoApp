package com.example.gastoapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gastoapp.R
import com.example.gastoapp.databinding.ItemTransaccionBinding
import com.example.gastoapp.model.TipoTransaccion
import com.example.gastoapp.model.Transaccion
import com.example.gastoapp.ui.FormatoTransaccion

// ListAdapter calcula las diferencias cuando recibe una nueva lista con submitList.
class TransaccionAdapter : ListAdapter<Transaccion, TransaccionAdapter.TransaccionViewHolder>(Diferencias) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaccionViewHolder {
        val binding = ItemTransaccionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransaccionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransaccionViewHolder, position: Int) {
        holder.mostrar(getItem(position))
    }

    class TransaccionViewHolder(private val binding: ItemTransaccionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun mostrar(transaccion: Transaccion) {
            // Al reciclar una fila se asignan todos sus campos, no solo los que cambiaron.
            binding.textDescripcion.text = transaccion.descripcion
            binding.textMonto.text = FormatoTransaccion.monto(transaccion.monto)
            val colorMonto = if (transaccion.tipo == TipoTransaccion.INGRESO) {
                R.color.color_income
            } else {
                R.color.color_expense
            }
            // El color refuerza el tipo, pero la fila conserva todos sus textos identificadores.
            binding.textMonto.setTextColor(ContextCompat.getColor(binding.root.context, colorMonto))
            binding.textCategoria.text = transaccion.categoria
            binding.textFecha.text = FormatoTransaccion.fecha(transaccion.fecha)
        }
    }

    private object Diferencias : DiffUtil.ItemCallback<Transaccion>() {
        override fun areItemsTheSame(anterior: Transaccion, nueva: Transaccion): Boolean =
            anterior.id == nueva.id

        override fun areContentsTheSame(anterior: Transaccion, nueva: Transaccion): Boolean =
            anterior == nueva
    }
}
