package com.example.motorc.modelos
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Usuario(
    var id: String? = "",
    var email: String? = "",
    var nombre: String? = "",
    var foto_url: String? = "",
    var direccion: String? = "",
    var admin: Boolean? = false,
    var fecha_creacion: String? = "",
    var contrasena: String? = "",
    var disponible: Boolean = true
): Parcelable