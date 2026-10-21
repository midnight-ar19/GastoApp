package com.example.gastoapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gastoapp.model.DatosEjemplo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {
    // La precarga ocurre una sola vez por instancia del ViewModel, no por Fragment.
    private val _estado = MutableStateFlow(
        EstadoFinanciero(DatosEjemplo.crearTransacciones())
    )

    // Las pantallas pueden observar el estado, pero no asignarle nuevos valores.
    val estado: StateFlow<EstadoFinanciero> = _estado.asStateFlow()

    // En la fase 6 agregaremos el alta validada desde el diálogo.
    // El estado vive solo en memoria: sobrevive a rotaciones, no a la muerte del proceso.
}
