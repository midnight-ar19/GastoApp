# Organización de la aplicación educativa

La fase 1 habilita ViewBinding y declara las dependencias. La fase 2 conecta
la Activity con tres Fragments de contenido provisional para probar navegación.
Las listas, los datos y las pestañas se implementarán en sus fases respectivas.

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

## Git

Cada fase se desarrolla en una rama `feat/phase-N-...`. No se publica ni se
fusiona automáticamente. La fase siguiente debe partir del resultado aprobado
de la anterior para conservar su configuración y comportamiento.
