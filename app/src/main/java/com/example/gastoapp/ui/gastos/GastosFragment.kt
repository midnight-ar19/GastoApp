package com.example.gastoapp.ui.gastos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gastoapp.R
import com.example.gastoapp.databinding.FragmentGastosBinding
import com.example.gastoapp.model.TipoTransaccion
import com.example.gastoapp.ui.base.BaseFragment
import com.example.gastoapp.ui.dialog.DialogoTransaccion
import com.example.gastoapp.viewmodel.SharedViewModel
import com.google.android.material.tabs.TabLayoutMediator

class GastosFragment : BaseFragment() {
    private var _binding: FragmentGastosBinding? = null
    private val viewModel: SharedViewModel by activityViewModels()
    private var tabLayoutMediator: TabLayoutMediator? = null
    private var dialogoAlta: DialogoTransaccion? = null

    override fun crearVista(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // El Fragment puede sobrevivir a su vista: creamos un binding por cada vista.
        val binding = FragmentGastosBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = requireNotNull(_binding)
        binding.viewPagerGastos.adapter = GastosPagerAdapter(this)
        // El Mediator se conecta una sola vez a los controles de esta vista.
        tabLayoutMediator = TabLayoutMediator(
            binding.tabLayoutGastos,
            binding.viewPagerGastos
        ) { tab, position ->
            tab.text = getString(
                if (position == 0) R.string.tab_gastos_este_mes
                else R.string.tab_gastos_anteriores
            )
        }.also { it.attach() }
        binding.fabAgregarGasto.setOnClickListener {
            dialogoAlta?.dismiss()
            dialogoAlta = DialogoTransaccion(
                contexto = requireContext(),
                viewModel = viewModel,
                tipo = TipoTransaccion.GASTO
            ).also { it.mostrar() }
        }
    }

    override fun onDestroyView() {
        // Cerramos el formulario antes de destruir la vista del Fragment padre.
        dialogoAlta?.dismiss()
        dialogoAlta = null
        // Primero deshacemos la conexión y después liberamos el adaptador y el binding.
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        _binding?.viewPagerGastos?.adapter = null
        super.onDestroyView()
        _binding = null
    }

    private class GastosPagerAdapter(parentFragment: Fragment) :
        FragmentStateAdapter(parentFragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment =
            ListaGastosFragment.newInstance(
                if (position == 0) PeriodoGastos.ESTE_MES else PeriodoGastos.ANTERIORES
            )
    }
}
