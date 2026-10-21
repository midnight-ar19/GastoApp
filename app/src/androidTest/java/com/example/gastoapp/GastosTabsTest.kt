package com.example.gastoapp

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.tabs.TabLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GastosTabsTest {
    @Test
    fun muestraLasDosPestanasYLosGastosDelMesActual() {
        ActivityScenario.launch(MainActivity::class.java).use {
            irAGastos()

            onView(withText("Este mes")).check(matches(isDisplayed()))
            onView(withText("Anteriores")).check(matches(isDisplayed()))
            verificaPestanaSeleccionada("Este mes")
            onView(withText("Supermercado")).check(matches(isDisplayed()))
            onView(withText("Colectivo")).check(matches(isDisplayed()))
        }
    }

    @Test
    fun cambiaALosGastosAnterioresAlPulsarLaPestana() {
        ActivityScenario.launch(MainActivity::class.java).use {
            irAGastos()
            onView(withText("Anteriores")).perform(click())

            verificaPestanaSeleccionada("Anteriores")
            onView(withText("Servicio de internet")).check(matches(isDisplayed()))
        }
    }

    @Test
    fun cambiaALosGastosAnterioresAlDeslizarElViewPager() {
        ActivityScenario.launch(MainActivity::class.java).use {
            irAGastos()
            onView(withId(R.id.view_pager_gastos)).perform(swipeLeft())

            verificaPestanaSeleccionada("Anteriores")
            onView(withText("Servicio de internet")).check(matches(isDisplayed()))
        }
    }

    @Test
    fun conservaLaPestanaSeleccionadaAlRecrearLaActivity() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            irAGastos()
            onView(withText("Anteriores")).perform(click())
            onView(withText("Servicio de internet")).check(matches(isDisplayed()))

            scenario.recreate()

            verificaPestanaSeleccionada("Anteriores")
            onView(withText("Servicio de internet")).check(matches(isDisplayed()))
        }
    }

    @Test
    fun vuelveAGastosDesdeLaNavegacionInferior() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.ingresosFragment)).perform(click())
            onView(withId(R.id.gastosFragment)).perform(click())

            onView(withText("Este mes")).check(matches(isDisplayed()))
            onView(withText("Supermercado")).check(matches(isDisplayed()))
        }
    }

    private fun irAGastos() {
        onView(withId(R.id.gastosFragment)).perform(click())
    }

    private fun verificaPestanaSeleccionada(texto: String) {
        onView(withId(R.id.tab_layout_gastos)).check(matches(tabSeleccionada(texto)))
    }

    private fun tabSeleccionada(texto: String): Matcher<View> = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("una pestaña seleccionada con texto ").appendValue(texto)
        }

        override fun matchesSafely(view: View): Boolean {
            val tabLayout = view as? TabLayout ?: return false
            return (0 until tabLayout.tabCount).any { posicion ->
                val tab = tabLayout.getTabAt(posicion)
                tab?.isSelected == true && tab.text?.toString() == texto
            }
        }
    }
}
