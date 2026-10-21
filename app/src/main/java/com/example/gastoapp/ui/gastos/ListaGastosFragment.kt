package com.example.gastoapp.ui.gastos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastoapp.databinding.FragmentListaGastosBinding
import com.example.gastoapp.ui.adapter.TransaccionAdapter
import com.example.gastoapp.ui.base.BaseFragment
import com.example.gastoapp.viewmodel.SharedViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Cada página conserva su período en argumentos para que FragmentManager pueda restaurarla.
class ListaGastosFragment : BaseFragment() {
    private var _binding: FragmentListaGastosBinding? = null
    private val viewModel: SharedViewModel by activityViewModels()

    private val periodo: PeriodoGastos
        get() = PeriodoGastos.valueOf(
            requireNotNull(requireArguments().getString(ARG_PERIODO))
        )

    override fun crearVista(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentListaGastosBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = requireNotNull(_binding)
        val adapter = TransaccionAdapter().apply {
            stateRestorationPolicy = RecyclerView.Adapter
                .StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        binding.listaGastos.layoutManager = LinearLayoutManager(requireContext())
        // El FAB vive en el Fragment padre y se superpone a la lista.
        val espacioFab = (96 * resources.displayMetrics.density).roundToInt()
        binding.listaGastos.setPadding(
            binding.listaGastos.paddingLeft,
            binding.listaGastos.paddingTop,
            binding.listaGastos.paddingRight,
            espacioFab
        )
        binding.listaGastos.clipToPadding = false
        binding.listaGastos.adapter = adapter
        // Una referencia por vista mantiene los límites consistentes mientras se observa el estado.
        val referencia = System.currentTimeMillis()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    // La página proyecta gastos y período sobre la lista filtrada.
                    adapter.submitList(
                        seleccionarGastos(estado.transaccionesVisibles, periodo, referencia)
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        // Desconectamos el adaptador antes de liberar el RecyclerView de esta vista.
        _binding?.listaGastos?.adapter = null
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PERIODO = "periodo"

        fun newInstance(periodo: PeriodoGastos): ListaGastosFragment =
            ListaGastosFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PERIODO, periodo.name)
                }
            }
    }
}
