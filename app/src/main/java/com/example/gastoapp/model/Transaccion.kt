package com.example.gastoapp.model

import java.math.BigDecimal

// Un enum evita representar los tipos con textos que podrían escribirse distinto.
enum class TipoTransaccion {
    INGRESO,
    GASTO
}

// Los campos son de solo lectura: una transacción representa un dato, no una vista.
data class Transaccion(
    val id: Long,
    val descripcion: String,
    // Usamos valores decimales exactos, creados desde String y nunca desde Double.
    val monto: BigDecimal,
    val categoria: String,
    // Milisegundos desde el inicio de la época Unix; compatible con minSdk 24.
    val fecha: Long,
    val tipo: TipoTransaccion
)
