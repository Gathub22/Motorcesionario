package com.example.motorc.modelos

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class MensajePedido(
    val id: String = "",
    val id_pedido: String = "",
    val emisor_id: String = "",
    val emisor_foto: String = "",
    val fecha: String = "",
    val contenido: String = ""
):Parcelable