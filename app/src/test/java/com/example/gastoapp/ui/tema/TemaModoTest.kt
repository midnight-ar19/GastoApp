package com.example.gastoapp.ui.tema

import androidx.appcompat.app.AppCompatDelegate
import org.junit.Assert.assertEquals
import org.junit.Test

class TemaModoTest {
    @Test
    fun modoClaroUsaLaBanderaSinNocheDeAppCompat() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ModoTema.CLARO.modoNoche)
    }

    @Test
    fun modoOscuroUsaLaBanderaDeNocheDeAppCompat() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ModoTema.OSCURO.modoNoche)
    }

    @Test
    fun ausenciaDePreferenciaConservaElModoClaro() {
        assertEquals(ModoTema.CLARO, ModoTema.desdeValor(null))
    }

    @Test
    fun valorDesconocidoConservaElModoClaro() {
        assertEquals(ModoTema.CLARO, ModoTema.desdeValor("modo-inexistente"))
    }

    @Test
    fun losValoresPersistidosSonEstables() {
        assertEquals("claro", ModoTema.CLARO.valorPersistido)
        assertEquals("oscuro", ModoTema.OSCURO.valorPersistido)
    }
}
