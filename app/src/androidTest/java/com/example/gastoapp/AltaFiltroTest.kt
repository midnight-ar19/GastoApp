package com.example.gastoapp

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
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
            onView(withId(R.id.lista_ingresos)).check(
                matches(conteoDeFilas(0))
            )
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
            onView(withId(R.id.lista_ingresos)).check(matches(conteoDeFilas(1)))
            onView(withText("Clase particular")).check(matches(isDisplayed()))

            abrirFiltro()
            seleccionarCategoria("Todas las categorías")
            onView(withId(R.id.lista_ingresos)).check(matches(conteoDeFilas(2)))
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

            onView(withId(R.id.lista_ingresos)).check(matches(conteoDeFilas(2)))
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

            onView(withId(R.id.lista_ingresos)).check(matches(conteoDeFilas(2)))
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
}
