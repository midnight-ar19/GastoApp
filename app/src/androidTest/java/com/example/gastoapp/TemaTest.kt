package com.example.gastoapp

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gastoapp.ui.tema.ModoTema
import com.example.gastoapp.ui.tema.TemaPreferencias
import com.google.android.material.tabs.TabLayout
import java.math.BigDecimal
import java.text.NumberFormat
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TemaTest {
    @Test
    fun cambiaEntreClaroYOscuroDesdeLaAccionPublica() {
        val modoInicial = TemaPreferencias.leer(contextoPrueba())
        val escenario = iniciarEn(ModoTema.CLARO)
        try {
            escenario.onActivity { assertTheme(it, ModoTema.CLARO) }

            abrirSelectorTema()
            onView(withText("Oscuro")).perform(click())
            escenario.onActivity { assertTheme(it, ModoTema.OSCURO) }

            abrirSelectorTema()
            onView(withText("Claro")).perform(click())
            escenario.onActivity { assertTheme(it, ModoTema.CLARO) }
        } finally {
            escenario.close()
            restaurar(modoInicial)
        }
    }

    @Test
    fun cancelarElSelectorNoCambiaElModo() {
        val modoInicial = TemaPreferencias.leer(contextoPrueba())
        val escenario = iniciarEn(ModoTema.CLARO)
        try {
            abrirSelectorTema()
            onView(withText("Oscuro")).check(matches(isDisplayed()))
            onView(withText("Cancelar")).perform(click())

            escenario.onActivity { assertTheme(it, ModoTema.CLARO) }
            assertEquals(ModoTema.CLARO, TemaPreferencias.leer(contextoPrueba()))
        } finally {
            escenario.close()
            restaurar(modoInicial)
        }
    }

    @Test
    fun conservaLaPreferenciaAlRecrearYVolverALanzar() {
        val modoInicial = TemaPreferencias.leer(contextoPrueba())
        var relanzada: ActivityScenario<MainActivity>? = null
        var escenarioCerrado = false
        val escenario = iniciarEn(ModoTema.CLARO)
        try {
            abrirSelectorTema()
            onView(withText("Oscuro")).perform(click())
            escenario.onActivity { assertTheme(it, ModoTema.OSCURO) }

            escenario.recreate()
            escenario.onActivity { assertTheme(it, ModoTema.OSCURO) }
            escenario.close()
            escenarioCerrado = true

            relanzada = ActivityScenario.launch(MainActivity::class.java)
            relanzada.onActivity { assertTheme(it, ModoTema.OSCURO) }
        } finally {
            if (!escenarioCerrado) {
                escenario.close()
            }
            relanzada?.close()
            restaurar(modoInicial)
        }
    }

    @Test
    fun conservaDatosFiltroDestinoYPestanaAlCambiarElTema() {
        val modoInicial = TemaPreferencias.leer(contextoPrueba())
        val escenario = iniciarEn(ModoTema.CLARO)
        try {
            onView(withId(R.id.gastosFragment)).perform(click())
            onView(withId(R.id.fab_agregar_gasto)).perform(click())
            onView(withId(R.id.campo_monto)).perform(replaceText("4.00"))
            onView(withId(R.id.campo_descripcion)).perform(replaceText("Gasto persistente"))
            onView(withId(R.id.campo_categoria)).perform(
                replaceText("Persistencia"),
                closeSoftKeyboard()
            )
            onView(withText("Guardar")).perform(click())

            onView(withText("Filtrar")).perform(click())
            onView(withText("Persistencia")).perform(click())
            onView(withText("Gasto persistente")).check(matches(isDisplayed()))
            onView(withText("Anteriores")).perform(click())

            abrirSelectorTema()
            onView(withText("Oscuro")).perform(click())
            escenario.onActivity { activity ->
                assertEquals(
                    "Gastos",
                    activity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
                        .title
                        .toString()
                )
                assertEquals(Configuration.UI_MODE_NIGHT_YES,
                    activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
            }
            onView(withId(R.id.toolbar)).check(
                matches(hasDescendant(withText("Filtro de listas: Persistencia")))
            )
            onView(withId(R.id.tab_layout_gastos)).check(matches(tabSeleccionada("Anteriores")))

            onView(withText("Este mes")).perform(click())
            onView(withText("Gasto persistente")).check(matches(isDisplayed()))
            onView(withText("Anteriores")).perform(click())
            onView(withId(R.id.resumenFragment)).perform(click())
            onView(withText(monto("294.50"))).check(matches(isDisplayed()))
            onView(withText(monto("955.50"))).check(matches(isDisplayed()))
            onView(withId(R.id.gastosFragment)).perform(click())
            onView(withId(R.id.tab_layout_gastos)).check(matches(tabSeleccionada("Anteriores")))
            onView(withId(R.id.toolbar)).check(
                matches(hasDescendant(withText("Filtro de listas: Persistencia")))
            )
        } finally {
            escenario.close()
            restaurar(modoInicial)
        }
    }

    private fun abrirSelectorTema() {
        // La acción ahora es un icono; el ID sigue siendo el mismo en ambos modos.
        onView(withId(R.id.menu_tema)).check(matches(tieneIconoTema())).perform(click())
    }

    private fun tieneIconoTema(): Matcher<View> = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("una acción de tema con icono de sol o luna")
        }

        override fun matchesSafely(view: View): Boolean =
            (view as? TextView)?.compoundDrawables?.any { it != null } == true
    }

    private fun iniciarEn(modo: ModoTema): ActivityScenario<MainActivity> {
        restaurar(modo)
        return ActivityScenario.launch(MainActivity::class.java)
    }

    private fun restaurar(modo: ModoTema) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            TemaPreferencias.guardarYAplicar(contextoPrueba(), modo)
        }
    }

    private fun contextoPrueba() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun assertTheme(activity: MainActivity, modo: ModoTema) {
        val configuracion = activity.resources.configuration
        val noche = configuracion.uiMode and Configuration.UI_MODE_NIGHT_MASK
        assertEquals(
            if (modo == ModoTema.OSCURO) Configuration.UI_MODE_NIGHT_YES
            else Configuration.UI_MODE_NIGHT_NO,
            noche
        )

        val fondo = (activity.findViewById<View>(R.id.main).background as ColorDrawable).color
        assertEquals(
            if (modo == ModoTema.OSCURO) Color.BLACK else Color.rgb(242, 242, 247),
            fondo
        )
        assertEquals(
            if (modo == ModoTema.OSCURO) Color.rgb(28, 28, 30) else Color.WHITE,
            ContextCompat.getColor(activity, R.color.color_surface)
        )
        assertEquals(
            ContextCompat.getColor(activity, R.color.color_balance),
            activity.findViewById<TextView>(R.id.text_balance).currentTextColor
        )
        assertEquals(
            ContextCompat.getColor(activity, R.color.color_income),
            activity.findViewById<TextView>(R.id.text_total_ingresos).currentTextColor
        )
        assertEquals(
            ContextCompat.getColor(activity, R.color.color_expense),
            activity.findViewById<TextView>(R.id.text_total_gastos).currentTextColor
        )
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

    private fun monto(valor: String): String = NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(BigDecimal(valor))
}
