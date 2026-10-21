package com.example.gastoapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.gastoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // El binding genera referencias seguras a las vistas de activity_main.xml.
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Conservamos los márgenes seguros para las barras del sistema.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // El host ya fue creado al inflar el XML: obtenemos su controlador.
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        // Son secciones principales: ninguna muestra una flecha de subir.
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.resumenFragment, R.id.ingresosFragment, R.id.gastosFragment)
        )
        // NavigationUI actualiza el título y administra selección y pilas de navegación.
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
    }
}
