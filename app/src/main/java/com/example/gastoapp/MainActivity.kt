package com.example.gastoapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.gastoapp.databinding.ActivityMainBinding
import com.example.gastoapp.ui.dialog.DialogoFiltroCategoria
import com.example.gastoapp.ui.tema.DialogoTema
import com.example.gastoapp.ui.tema.TemaPreferencias
import com.example.gastoapp.viewmodel.SharedViewModel
import com.example.gastoapp.viewmodel.EstadoFinanciero
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val viewModel: SharedViewModel by viewModels()
    private var dialogoFiltro: AlertDialog? = null
    private var dialogoTema: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // AppCompat debe conocer el modo antes de inflar cualquier vista para evitar un destello.
        TemaPreferencias.aplicarGuardado(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // El binding genera referencias seguras a las vistas de activity_main.xml.
        val binding = ActivityMainBinding.inflate(layoutInflater)
        _binding = binding
        setContentView(binding.root)
        // La Activity necesita administrar el menú de la Toolbar además de su título.
        setSupportActionBar(binding.toolbar)

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
        // Al registrar la Toolbar como ActionBar, Navigation debe actualizar esa ActionBar.
        // Conectarla directamente a la Toolbar permitiría que AppCompat sobrescriba el título.
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)

        // El subtítulo identifica el filtro de listas incluso cuando se ve Resumen.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    actualizarSubtitulo(estado)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_filtrar -> {
            mostrarDialogoFiltro()
            true
        }
        R.id.menu_tema -> {
            mostrarDialogoTema()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun mostrarDialogoTema() {
        dialogoTema?.dismiss()
        val dialogo = DialogoTema.mostrar(
            contexto = this,
            modoActual = TemaPreferencias.leer(this),
            alSeleccionar = { modo ->
                // La preferencia se escribe antes de pedir a AppCompat la recreación.
                TemaPreferencias.guardarYAplicar(this, modo)
            }
        )
        dialogoTema = dialogo
        dialogo.setOnDismissListener {
            if (dialogoTema === dialogo) {
                dialogoTema = null
            }
        }
    }

    private fun actualizarSubtitulo(estado: EstadoFinanciero) {
        val categoria = estado.categoriaSeleccionada
        _binding?.toolbar?.subtitle = categoria?.let {
            getString(R.string.subtitulo_filtro_listas, it)
        }
    }

    private fun mostrarDialogoFiltro() {
        dialogoFiltro?.dismiss()
        val dialogo = DialogoFiltroCategoria.mostrar(
            contexto = this,
            categorias = viewModel.estado.value.categoriasDisponibles,
            categoriaSeleccionada = viewModel.estado.value.categoriaSeleccionada,
            alSeleccionar = { categoria ->
                viewModel.seleccionarCategoria(categoria)
                dialogoFiltro = null
            }
        )
        dialogoFiltro = dialogo
        dialogo.setOnDismissListener {
            if (dialogoFiltro === dialogo) {
                dialogoFiltro = null
            }
        }
    }

    override fun onDestroy() {
        // Los diálogos pertenecen a esta ventana: se cierran antes de destruir la Activity.
        dialogoFiltro?.dismiss()
        dialogoFiltro = null
        dialogoTema?.dismiss()
        dialogoTema = null
        _binding = null
        super.onDestroy()
    }
}
