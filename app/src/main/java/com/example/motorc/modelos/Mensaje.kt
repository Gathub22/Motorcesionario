package com.example.motorc.modelos

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Mensaje(
    val id: String = "",
    val emisor_id: String = "",
    val emisor_foto: String = "",
    val fecha: String = "",
    val contenido: String = ""
):Parcelable