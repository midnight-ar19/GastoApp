package com.example.gastoapp.ui.base

import com.example.gastoapp.ui.gastos.GastosFragment
import com.example.gastoapp.ui.gastos.ListaGastosFragment
import com.example.gastoapp.ui.ingresos.IngresosFragment
import com.example.gastoapp.ui.resumen.ResumenFragment
import org.junit.Assert.assertTrue
import org.junit.Test

class BaseFragmentTest {
    @Test
    fun todosLosFragmentsDeLaAplicacionHeredanDeBaseFragment() {
        val fragments = listOf(
            ResumenFragment::class.java,
            IngresosFragment::class.java,
            GastosFragment::class.java,
            ListaGastosFragment::class.java
        )

        fragments.forEach { fragmentClass ->
            assertTrue(
                "${fragmentClass.simpleName} debe heredar de BaseFragment",
                BaseFragment::class.java.isAssignableFrom(fragmentClass)
            )
        }
    }
}
