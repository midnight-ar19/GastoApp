package com.example.gastoapp.ui.resumen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gastoapp.databinding.FragmentResumenBinding
import com.example.gastoapp.ui.FormatoTransaccion
import com.example.gastoapp.ui.base.BaseFragment
import com.example.gastoapp.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

class ResumenFragment : BaseFragment() {
    private var _binding: FragmentResumenBinding? = null
    // El propietario es la Activity: Ingresos obtiene exactamente la misma instancia.
    private val viewModel: SharedViewModel by activityViewModels()

    override fun crearVista(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentResumenBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = requireNotNull(_binding)
        // La colección se cancela al destruir la vista y se pausa cuando no está STARTED.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    binding.textBalance.text = FormatoTransaccion.monto(estado.balance)
                    binding.textTotalIngresos.text = FormatoTransaccion.monto(estado.totalIngresos)
                    binding.textTotalGastos.text = FormatoTransaccion.monto(estado.totalGastos)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // El Fragment puede sobrevivir a su vista: no conservamos sus controles.
        _binding = null
    }
}
