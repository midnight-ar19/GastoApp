package com.example.gastoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @Test
    fun iniciaEnResumen() {
        ActivityScenario.launch(MainActivity::class.java).use {
            verificaDestino(R.string.title_resumen, R.string.placeholder_resumen)
        }
    }

    @Test
    fun navegaEntreLasTresSecciones() {
        ActivityScenario.launch(MainActivity::class.java).use {
            // Pulsamos los elementos reales de la barra inferior.
            onView(withId(R.id.ingresosFragment)).perform(click())
            verificaDestino(R.string.title_ingresos, R.string.placeholder_ingresos)
            onView(withId(R.id.gastosFragment)).perform(click())
            verificaDestino(R.string.title_gastos, R.string.placeholder_gastos)
            onView(withId(R.id.resumenFragment)).perform(click())
            verificaDestino(R.string.title_resumen, R.string.placeholder_resumen)
        }
    }

    @Test
    fun conservaElDestinoAlRecrearLaActivity() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.gastosFragment)).perform(click())
            scenario.recreate()
            verificaDestino(R.string.title_gastos, R.string.placeholder_gastos)
        }
    }

    private fun verificaDestino(titulo: Int, contenido: Int) {
        // Comprobamos tanto el título dinámico como el contenido visible.
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(titulo))))
        onView(withText(contenido)).check(matches(isDisplayed()))
    }
}
