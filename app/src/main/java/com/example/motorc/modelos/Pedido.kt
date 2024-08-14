package com.example.motorc.modelos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pedido (
   var id: String = "",
   var img_prod: String = "",
   var id_prod: String = "",
   var nom_prod: String = "",
   var mensaje: String = "",
   var estado: Int = 2,          /* 0: Pendiente, 1: Aceptado, 2: Enviado, 3: Recibido, 4: Rechazado */
   var cliente_id: String = "",
   var cliente_nom: String = "",
   var cliente_img: String = ""
): Parcelable