package com.example.gastoapp.ui.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Fragmento base que deja visible en Logcat el recorrido de cada pantalla.
 *
 * onCreateView es final a propósito: si cada hijo la sobrescribiera, podría olvidar llamar
 * a super y el registro común se perdería. En su lugar usamos el patrón método plantilla:
 * esta clase registra el evento y delega solamente la inflación concreta en crearVista.
 */
abstract class BaseFragment : Fragment() {
    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CicloVida", "${javaClass.simpleName}.onCreate")
    }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("CicloVida", "${javaClass.simpleName}.onCreateView")
        return crearVista(inflater, container, savedInstanceState)
    }

    /** Cada pantalla conserva aquí su inflación y configuración de binding. */
    protected abstract fun crearVista(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CicloVida", "${javaClass.simpleName}.onViewCreated")
    }

    final override fun onStart() {
        super.onStart()
        Log.d("CicloVida", "${javaClass.simpleName}.onStart")
    }

    final override fun onResume() {
        super.onResume()
        Log.d("CicloVida", "${javaClass.simpleName}.onResume")
    }

    final override fun onPause() {
        super.onPause()
        Log.d("CicloVida", "${javaClass.simpleName}.onPause")
    }

    final override fun onStop() {
        super.onStop()
        Log.d("CicloVida", "${javaClass.simpleName}.onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("CicloVida", "${javaClass.simpleName}.onDestroyView")
    }

    final override fun onDestroy() {
        super.onDestroy()
        Log.d("CicloVida", "${javaClass.simpleName}.onDestroy")
    }
}
