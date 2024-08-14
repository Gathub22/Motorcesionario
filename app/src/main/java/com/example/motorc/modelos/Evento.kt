package com.example.motorc.modelos

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Evento (
    var id: String,
    var img_url: String,
): Parcelable
