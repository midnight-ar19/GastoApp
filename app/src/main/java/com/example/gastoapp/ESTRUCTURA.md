# Organización de la aplicación educativa

La fase 1 habilita ViewBinding y declara las dependencias. La fase 2 conecta
la Activity con tres Fragments para probar navegación. La fase 3 agrega el
modelo y el estado; la fase 4 muestra tarjetas de Resumen y la lista de Ingresos.
La fase 5 organiza Gastos en pestañas con sus listas hijas.

## Paquetes previstos

- `com.example.gastoapp`: MainActivity, punto de entrada.
- `model`: Transaccion y TipoTransaccion (fase 3).
- `viewmodel`: SharedViewModel, datos en memoria y totales (fase 3).
- `ui/resumen`: ResumenFragment (fase 4).
- `ui/ingresos`: IngresosFragment (fase 4).
- `ui/adapter`: adaptador de transacciones (fase 4).
- `ui/gastos`: GastosFragment, páginas hijas y adaptador de páginas (fase 5).
- `ui/dialog`: diálogo de alta de transacciones (fase 6).
- `ui/base`: BaseFragment y registros del ciclo de vida (fase 7).

## Recursos previstos

- `res/layout`: vistas XML de Activity, Fragments, filas y diálogos.
- `res/navigation`: grafo de navegación (fase 2).
- `res/menu`: navegación inferior (fase 2) y filtro (fase 6).
- `res/values`: textos, colores y temas.

## Conceptos de la fase 1

`gradle/libs.versions.toml` centraliza las versiones y los alias de las librerías.
`app/build.gradle.kts` declara cuáles usa el módulo Android.
`buildFeatures.viewBinding` genera referencias tipadas a los controles de cada
layout; todavía no cambia cómo MainActivity infla su XML.

Navigation administra los destinos; Fragment KTX permite compartir un ViewModel
con `activityViewModels`; LiveData permite observar los datos con un propietario
de ciclo de vida. RecyclerView muestra listas reutilizando vistas. ViewPager2
muestra páginas y Material aporta Toolbar, BottomNavigationView, TabLayout y FAB.
CardView aporta las tarjetas solicitadas.

Se conserva la configuración original de SDK y Android Gradle Plugin. No se
agrega Compose, base de datos, cliente HTTP ni plugin Kotlin adicional: AGP 9
incluye soporte Kotlin integrado.

## Fase 2: navegación

`activity_main.xml` organiza Toolbar, NavHostFragment y BottomNavigationView.
`navigation/nav_graph.xml` define los tres destinos y sus títulos. Los IDs de
`menu/bottom_navigation_menu.xml` coinciden con los del grafo: NavigationUI usa
esa coincidencia para navegar sin transacciones manuales de Fragments.

MainActivity obtiene el NavController del host y conecta ambas barras con
`setupWithNavController`. AppBarConfiguration identifica las tres secciones
como principales, sin flecha de subir. No se llama a `navigate` al recrear la
Activity: Navigation restaura su estado sin volver a forzar Resumen.

Cada Fragment infla su propio XML con ViewBinding y libera el binding en
`onDestroyView`, porque el Fragment y su vista tienen ciclos de vida distintos.
BaseFragment se incorporará en la fase 7. El menú Filtrar y su comportamiento
se incorporarán juntos en la fase 6; no hay acciones que simulen filtrar.

Para comprobarlo en un dispositivo o emulador: iniciar en Resumen, tocar
Ingresos y Gastos, comprobar títulos y selección inferior, volver a Resumen
y rotar desde Gastos para comprobar la restauración. NavigationTest automatiza
el destino inicial, los cambios de sección y la recreación de la Activity.

## Fase 3: modelo y estado

- `model/Transaccion.kt`: define los seis campos y el enum INGRESO/GASTO.
- `model/DatosEjemplo.kt`: crea cinco movimientos con fechas relativas al mes
  actual y al anterior, con IDs diferentes.
- `viewmodel/EstadoFinanciero.kt`: calcula ingresos, gastos y balance desde
  una única lista. El balance incluye todo el historial, no solo el mes actual.
- `viewmodel/SharedViewModel.kt`: precarga y conserva el estado en memoria;
  expone StateFlow de solo lectura y mantiene MutableStateFlow privado.

Los montos usan BigDecimal construido desde String para evitar errores de
punto flotante. Los gastos se representan como importes positivos y se restan
al calcular el balance. Los valores de ejemplo no presuponen una moneda.
Las fechas usan milisegundos Unix y Calendar para ser compatibles con API 24.

Ejemplos: ingresos 1250.00, gastos 290.50, balance 959.50. Hay gastos del mes
actual y del anterior para las pestañas de la fase 5.

Desde la fase 4 los Fragments obtienen la misma instancia mediante
`private val viewModel: SharedViewModel by activityViewModels()` y observarán
`estado` con el ciclo de vida de su vista. El nombre SharedViewModel no hace
que sea compartido automáticamente: el propietario debe ser la Activity.
La fase 3 no cambia las pantallas; la fase 4 conecta Resumen e Ingresos.
El alta y su validación se implementarán con el diálogo en la fase 6.

Un ViewModel sobrevive a la recreación de la Activity, pero no a la muerte del
proceso. No hay persistencia, base de datos, SavedStateHandle ni llamadas HTTP.
Las pruebas unitarias cubren totales decimales exactos, lista vacía, balance
negativo y precarga con ambos tipos y períodos.

## Fase 4: tarjetas y RecyclerView

`fragment_resumen.xml` contiene tres CardView en un contenedor desplazable.
ResumenFragment observa el estado y muestra balance, total de ingresos y total
de gastos. El desplazamiento permite usar tamaños de texto mayores.

`fragment_ingresos.xml` contiene RecyclerView. LinearLayoutManager organiza sus
filas en vertical; TransaccionAdapter infla `item_transaccion.xml` con ViewBinding
y enlaza descripción, monto, categoría y fecha. ListAdapter recibe listas con
`submitList` y DiffUtil distingue identidad (id) de contenido (data class).
No se cambia la lista del ViewModel al seleccionar solo INGRESO.

Ambos Fragments usan activityViewModels: la Activity conserva una única instancia.
`viewLifecycleOwner.lifecycleScope` cancela la colección al destruir la vista;
`repeatOnLifecycle(STARTED)` la detiene cuando la vista no está activa y vuelve
a recoger el estado al mostrarse. Al salir se libera el binding y se desconecta
el adaptador. No se guardan referencias a vistas dentro del ViewModel.

FormatoTransaccion presenta montos con dos decimales y fechas según la
configuración del dispositivo. No agrega símbolo de moneda porque no se definió
una moneda. Las operaciones siguen usando BigDecimal, nunca el texto mostrado.

El FAB y el diálogo de alta se incorporarán juntos en la fase 6. BaseFragment
sigue reservado para la fase 7. ResumenIngresosTest comprueba tarjetas y
totales, las dos filas de ingresos con sus datos, exclusión de gastos y
recreación sin filas duplicadas. NavigationTest conserva las verificaciones de
navegación con el contenido real de estas pantallas.

## Fase 5: pestañas de gastos

`GastosFragment` contiene un `TabLayout` y un `ViewPager2`. Su
`FragmentStateAdapter` recibe el Fragment padre y crea dos
`ListaGastosFragment` con argumentos restaurables: `ESTE_MES` y `ANTERIORES`.
El `TabLayoutMediator` se conecta una vez por vista y se desconecta en
`onDestroyView`; también se libera el adaptador del `ViewPager2`.

Cada página usa `activityViewModels` para observar la misma instancia de
`SharedViewModel`. La selección de períodos está en `PeriodoGastos.kt`: usa el
inicio local del mes actual de forma inclusiva, el inicio del siguiente mes de
forma exclusiva y solo conserva transacciones de tipo GASTO. La colección usa
`viewLifecycleOwner.lifecycleScope` con `repeatOnLifecycle(STARTED)` y el
RecyclerView se desconecta al destruir la vista. No se fuerza `currentItem`,
por lo que `ViewPager2` puede restaurar la pestaña seleccionada.

`GastosPeriodosTest` prueba los límites exactos y el cambio de diciembre a
enero; `GastosTabsTest` comprueba etiquetas, listas, deslizamiento, recreación y
regreso desde la navegación inferior. El FAB y el diálogo de alta siguen
reservados para la fase 6.

## Git

Cada fase se desarrolla en una rama `feat/phase-N-...`. No se publica ni se
fusiona automáticamente. La fase siguiente debe partir del resultado aprobado
de la anterior para conservar su configuración y comportamiento.
