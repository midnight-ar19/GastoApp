package com.example.gastoapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gastoapp.databinding.ItemTransaccionBinding
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
