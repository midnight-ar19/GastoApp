package com.example.gastoapp

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.recyclerview.widget.RecyclerView
import java.math.BigDecimal
import java.text.NumberFormat
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AltaFiltroTest {
    @Test
    fun agregarIngresoActualizaResumen() {
        ActivityScenario.launch(MainActivity::class.java).use {
            irAIngresos()
            abrirAlta(R.id.fab_agregar_ingreso)
            llenarAlta("35,50", "Honorarios UI", "Freelance")
            guardar()

            onView(withId(R.id.resumenFragment)).perform(click())
            onView(withText(monto("1285.50"))).check(matches(isDisplayed()))
            onView(withText(monto("995.00"))).check(matches(isDisplayed()))
        }
    }

    @Test
    fun agregarGastoActualizaPestanaActualYResumen() {
        ActivityScenario.launch(MainActivity::class.java).use {
            irAGastos()
            abrirAlta(R.id.fab_agregar_gasto)
            llenarAlta("15,25", "Gasto UI actual", "Pruebas UI")
            guardar()

            onView(withText("Gasto UI actual")).check(matches(isDisplayed()))
            onView(withId(R.id.resumenFragment)).perform(click())
            onView(withText(monto("305.75"))).check(matches(isDisplayed()))
            onView(withText(monto("944.25"))).check(matches(isDisplayed()))
        }
    }

    @Test
    fun filtroDeCategoriaAfectaIngresosAmbosPeriodosYNoResumen() {
        ActivityScenario.launch(MainActivity::class.java).use {
            irAGastos()
            abrirAlta(R.id.fab_agregar_gasto)
            llenarAlta("17.00", "Servicio del mes", "Servicios")
            guardar()
            abrirFiltro()
            seleccionarCategoria("Servicios")

            irAIngresos()
            esperarConteoDeFilas(R.id.lista_ingresos, 0)
            onView(withId(R.id.lista_ingresos)).check(matches(isDisplayed()))

            irAGastos()
            onView(withText("Este mes")).perform(click())
            onView(withText("Servicio del mes")).check(matches(isDisplayed()))
            onView(withText("Anteriores")).perform(click())
            onView(withText("Servicio de internet")).check(matches(isDisplayed()))

            onView(withId(R.id.resumenFragment)).perform(click())
            // Resumen conserva los totales globales aunque las listas estén filtradas.
            onView(withText(monto("1250.00"))).check(matches(isDisplayed()))
            onView(withText(monto("307.50"))).check(matches(isDisplayed()))
            onView(withText(monto("942.50"))).check(matches(isDisplayed()))
        }
    }

    @Test
    fun permiteLimpiarElFiltro() {
        ActivityScenario.launch(MainActivity::class.java).use {
            irAIngresos()
            abrirFiltro()
            seleccionarCategoria("Educación")
            esperarConteoDeFilas(R.id.lista_ingresos, 1)
            onView(withText("Clase particular")).check(matches(isDisplayed()))

            abrirFiltro()
            seleccionarCategoria("Todas las categorías")
            esperarConteoDeFilas(R.id.lista_ingresos, 2)
            onView(withText("Sueldo")).check(matches(isDisplayed()))
        }
    }

    @Test
    fun cancelarNoAgregaLaTransaccion() {
        ActivityScenario.launch(MainActivity::class.java).use {
            irAIngresos()
            abrirAlta(R.id.fab_agregar_ingreso)
            llenarAlta("99.00", "No se guarda", "Canceladas")
            onView(withText("Cancelar")).perform(click())

            esperarConteoDeFilas(R.id.lista_ingresos, 2)
            onView(withId(R.id.resumenFragment)).perform(click())
            onView(withText(monto("1250.00"))).check(matches(isDisplayed()))
        }
    }

    @Test
    fun formularioInvalidoMuestraErrorYPermaneceAbierto() {
        ActivityScenario.launch(MainActivity::class.java).use {
            irAIngresos()
            abrirAlta(R.id.fab_agregar_ingreso)
            llenarAlta("0", "Monto inválido", "Validación")
            guardar()

            // Mientras el modal está abierto, las comprobaciones se hacen en su propia ventana.
            onView(withText("Agregar ingreso")).check(matches(isDisplayed()))
            onView(withText("El monto debe ser mayor que cero.")).check(matches(isDisplayed()))
            onView(withText("Guardar")).check(matches(isDisplayed()))
            onView(withText("Cancelar")).check(matches(isDisplayed()))
            onView(withText("Cancelar")).perform(click())

            esperarConteoDeFilas(R.id.lista_ingresos, 2)
            onView(withId(R.id.resumenFragment)).perform(click())
            onView(withText(monto("1250.00"))).check(matches(isDisplayed()))
            onView(withText(monto("290.50"))).check(matches(isDisplayed()))
            onView(withText(monto("959.50"))).check(matches(isDisplayed()))
        }
    }

    @Test
    fun filtroYRegistroGuardadoSobrevivenLaRecreacion() {
        ActivityScenario.launch(MainActivity::class.java).use { escenario ->
            irAIngresos()
            abrirAlta(R.id.fab_agregar_ingreso)
            llenarAlta("8.00", "Guardado al rotar", "Recrear")
            guardar()
            abrirFiltro()
            seleccionarCategoria("Recrear")
            onView(withText("Guardado al rotar")).check(matches(isDisplayed()))

            escenario.recreate()

            onView(withId(R.id.toolbar)).check(
                matches(hasDescendant(withText("Filtro de listas: Recrear")))
            )
            onView(withText("Guardado al rotar")).check(matches(isDisplayed()))
        }
    }

    private fun irAIngresos() {
        onView(withId(R.id.ingresosFragment)).perform(click())
    }

    private fun irAGastos() {
        onView(withId(R.id.gastosFragment)).perform(click())
    }

    private fun abrirAlta(id: Int) {
        onView(withId(id)).perform(click())
    }

    private fun llenarAlta(monto: String, descripcion: String, categoria: String) {
        onView(withId(R.id.campo_monto)).perform(replaceText(monto))
        onView(withId(R.id.campo_descripcion)).perform(replaceText(descripcion))
        onView(withId(R.id.campo_categoria)).perform(replaceText(categoria), closeSoftKeyboard())
    }

    private fun guardar() {
        onView(withText("Guardar")).perform(click())
    }

    private fun abrirFiltro() {
        onView(withText("Filtrar")).perform(click())
    }

    private fun seleccionarCategoria(categoria: String) {
        onView(withText(categoria)).perform(click())
    }

    private fun esperarConteoDeFilas(idLista: Int, esperado: Int) {
        val recurso = ConteoFilasIdlingResource(esperado)
        onView(withId(idLista)).perform(recurso.conectarAction())
        try {
            IdlingRegistry.getInstance().register(recurso)
            onView(withId(idLista)).check(matches(conteoDeFilas(esperado)))
        } finally {
            IdlingRegistry.getInstance().unregister(recurso)
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                recurso.desconectar()
            }
        }
    }

    private fun monto(valor: String): String = NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(BigDecimal(valor))

    private fun conteoDeFilas(esperado: Int): Matcher<View> = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("una lista con $esperado filas")
        }

        override fun matchesSafely(view: View): Boolean =
            (view as? RecyclerView)?.adapter?.itemCount == esperado
    }

    /** Espera el evento público del adaptador, sin dormir ni consultar repetidamente. */
    private class ConteoFilasIdlingResource(
        private val esperado: Int
    ) : IdlingResource {
        private val observador = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() = actualizarEstado()

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = actualizarEstado()

            override fun onItemRangeChanged(
                positionStart: Int,
                itemCount: Int,
                payload: Any?
            ) = actualizarEstado()

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) =
                actualizarEstado()

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) =
                actualizarEstado()

            override fun onItemRangeMoved(
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) = actualizarEstado()
        }
        private var recyclerView: RecyclerView? = null
        private var actual = -1
        private var callback: IdlingResource.ResourceCallback? = null
        private var estaEnReposo = false

        override fun getName(): String = "ConteoFilasIdlingResource($esperado)"

        override fun isIdleNow(): Boolean = estaEnReposo

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
            this.callback = callback
            actualizarEstado()
        }

        fun conectarAction(): ViewAction = object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()

            override fun getDescription(): String = "conectar observador del conteo de filas"

            override fun perform(uiController: UiController, view: View) {
                conectar(view as RecyclerView)
            }
        }

        fun desconectar() {
            recyclerView?.adapter?.unregisterAdapterDataObserver(observador)
            recyclerView = null
            callback = null
        }

        private fun conectar(lista: RecyclerView) {
            recyclerView = lista
            lista.adapter?.registerAdapterDataObserver(observador)
            actualizarEstado()
        }

        private fun actualizarEstado() {
            actual = recyclerView?.adapter?.itemCount ?: -1
            if (actual == esperado && !estaEnReposo) {
                estaEnReposo = true
                callback?.onTransitionToIdle()
            }
        }
    }
}
