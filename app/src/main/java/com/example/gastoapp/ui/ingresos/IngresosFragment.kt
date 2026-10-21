package com.example.gastoapp.ui.ingresos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastoapp.databinding.FragmentIngresosBinding
import com.example.gastoapp.model.TipoTransaccion
import com.example.gastoapp.ui.adapter.TransaccionAdapter
import com.example.gastoapp.ui.dialog.DialogoTransaccion
import com.example.gastoapp.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

// En la fase 7 heredará de BaseFragment para registrar el ciclo de vida.
class IngresosFragment : Fragment() {
    private var _binding: FragmentIngresosBinding? = null
    private val viewModel: SharedViewModel by activityViewModels()
    private var dialogoAlta: DialogoTransaccion? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentIngresosBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = requireNotNull(_binding)
        // Un adaptador por vista: no retenemos un RecyclerView de una vista anterior.
        val adapter = TransaccionAdapter().apply {
            stateRestorationPolicy = androidx.recyclerview.widget.RecyclerView.Adapter
                .StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        binding.listaIngresos.layoutManager = LinearLayoutManager(requireContext())
        binding.listaIngresos.adapter = adapter
        binding.fabAgregarIngreso.setOnClickListener {
            dialogoAlta?.dismiss()
            dialogoAlta = DialogoTransaccion(
                contexto = requireContext(),
                viewModel = viewModel,
                tipo = TipoTransaccion.INGRESO
            ).also { it.mostrar() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    // La lista usa la proyección filtrada; los totales no pasan por aquí.
                    adapter.submitList(estado.transaccionesVisibles.filter {
                        it.tipo == TipoTransaccion.INGRESO
                    })
                }
            }
        }
    }

    override fun onDestroyView() {
        // Cerramos el formulario antes de liberar la vista que lo creó.
        dialogoAlta?.dismiss()
        dialogoAlta = null
        // Desconectamos el adaptador antes de liberar la vista del Fragment.
        _binding?.listaIngresos?.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
