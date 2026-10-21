package com.example.gastoapp.ui.resumen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gastoapp.databinding.FragmentResumenBinding

// En la fase 7 heredará de BaseFragment para registrar el ciclo de vida.
class ResumenFragment : Fragment() {
    private var _binding: FragmentResumenBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // El Fragment puede sobrevivir a su vista: creamos un binding por cada vista.
        val binding = FragmentResumenBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberamos la vista anterior para no retenerla en memoria.
        _binding = null
    }
}
