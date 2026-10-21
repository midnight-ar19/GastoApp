package com.example.gastoapp.ui.gastos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gastoapp.R
import com.example.gastoapp.databinding.FragmentGastosBinding
import com.google.android.material.tabs.TabLayoutMediator

// En la fase 7 heredará de BaseFragment para registrar el ciclo de vida.
class GastosFragment : Fragment() {
    private var _binding: FragmentGastosBinding? = null
    private var tabLayoutMediator: TabLayoutMediator? = null

    override fun onCreateView(
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
    }

    override fun onDestroyView() {
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
