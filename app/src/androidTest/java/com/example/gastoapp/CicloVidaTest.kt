package com.example.gastoapp

import android.os.ParcelFileDescriptor
import android.os.Process
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CicloVidaTest {
    @Test
    fun registraElCicloCompletoDeTodosLosFragments() {
        val marker = "PHASE7_BEGIN_${System.nanoTime()}"
        Log.d(TAG, marker)

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            // Recorremos las tres secciones y las dos páginas reales de ViewPager2.
            onView(withId(R.id.ingresosFragment)).perform(click())
            onView(withId(R.id.gastosFragment)).perform(click())
            onView(withText("Anteriores")).perform(click())
            onView(withText("Este mes")).perform(click())

            // CREATED representa la salida a segundo plano; RESUMED, el regreso al frente.
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)

            scenario.recreate()
            onView(withId(R.id.resumenFragment)).perform(click())
            onView(withId(R.id.ingresosFragment)).perform(click())
            onView(withId(R.id.gastosFragment)).perform(click())
            onView(withText("Anteriores")).perform(click())
        } finally {
            // close() lleva la Activity a DESTROYED y permite comprobar la limpieza final.
            scenario.close()
        }

        val logcatDesdeMarker = leerLogcat().substringAfter(marker, "")
        val callbacks = listOf(
            "onCreate",
            "onCreateView",
            "onViewCreated",
            "onStart",
            "onResume",
            "onPause",
            "onStop",
            "onDestroyView",
            "onDestroy"
        )
        val fragments = listOf(
            "ResumenFragment",
            "IngresosFragment",
            "GastosFragment",
            "ListaGastosFragment"
        )

        fragments.forEach { fragment ->
            callbacks.forEach { callback ->
                assertTrue(
                    "Falta $fragment.$callback después del marcador",
                    // Exigimos el final exacto: onCreateView no debe contar como onCreate.
                    logcatDesdeMarker.lineSequence().any {
                        it.trimEnd().endsWith("$fragment.$callback")
                    }
                )
            }
        }
    }

    private fun leerLogcat(): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand(
                "logcat -d -v brief --pid ${Process.myPid()} -s CicloVida:D '*:S'"
            )
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { reader ->
            reader.readText()
        }
    }

    private companion object {
        const val TAG = "CicloVida"
    }
}
