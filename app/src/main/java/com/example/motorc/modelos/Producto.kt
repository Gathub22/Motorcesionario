package com.example.motorc.modelos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto (
    var id: String = "",
    var disponible: Boolean = false,
    var nombre: String = "",
    var desc: String = "",
    var precio: Double = -1.0,
    var categoria: Int = 0,
    var foto: String = "",
): Parcelable