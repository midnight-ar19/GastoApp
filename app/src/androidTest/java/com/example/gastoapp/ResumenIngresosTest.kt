package com.example.gastoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.math.BigDecimal
import java.text.NumberFormat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResumenIngresosTest {
    @Test
    fun resumenMuestraLasTresTarjetasConTotales() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withText("Balance total")).check(matches(isDisplayed()))
            onView(withText("Total de ingresos")).check(matches(isDisplayed()))
            onView(withText("Total de gastos")).check(matches(isDisplayed()))
            onView(withText(monto("959.50"))).check(matches(isDisplayed()))
            onView(withText(monto("1250.00"))).check(matches(isDisplayed()))
            onView(withText(monto("290.50"))).check(matches(isDisplayed()))
        }
    }

    @Test
    fun ingresosMuestraSoloIngresosConSusDatos() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.ingresosFragment)).perform(click())
            verificaIngresos()
            // Los gastos del mismo ViewModel no deben aparecer en esta sección.
            onView(withText("Supermercado")).check(doesNotExist())
            onView(withText("Servicio de internet")).check(doesNotExist())
        }
    }

    @Test
    fun ingresosSeConservaSinDuplicadosAlRecrear() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.ingresosFragment)).perform(click())
            scenario.recreate()
            // Espresso exige una coincidencia única: una fila duplicada también falla.
            verificaIngresos()
        }
    }

    private fun verificaIngresos() {
        onView(withText("Sueldo")).check(matches(isDisplayed()))
        onView(withText("Trabajo")).check(matches(isDisplayed()))
        onView(withText(monto("1000.00"))).check(matches(isDisplayed()))
        onView(withText("Clase particular")).check(matches(isDisplayed()))
        onView(withText("Educación")).check(matches(isDisplayed()))
        onView(withText(monto("250.00"))).check(matches(isDisplayed()))
        // Ambos ejemplos pertenecen al primer día del mes actual.
        val fecha = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
        }.time
        val textoFecha = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(fecha)
        onView(withText("Sueldo")).check(matches(isDisplayed()))
        // Cada fila muestra una fecha; comprobamos ambas sin depender del orden visual.
        onView(org.hamcrest.Matchers.allOf(
            withText(textoFecha),
            androidx.test.espresso.matcher.ViewMatchers.hasSibling(withText("Sueldo"))
        )).check(matches(isDisplayed()))
        onView(org.hamcrest.Matchers.allOf(
            withText(textoFecha),
            androidx.test.espresso.matcher.ViewMatchers.hasSibling(withText("Clase particular"))
        )).check(matches(isDisplayed()))
    }

    private fun monto(valor: String): String = NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(BigDecimal(valor))
}
