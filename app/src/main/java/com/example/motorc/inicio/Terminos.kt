package com.example.motorc.inicio

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.motorc.R
import com.example.motorc.lib.Android.Companion.mostrarAviso

class Terminos : AppCompatActivity(){

    lateinit var bt_volver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terminos_uso)

        bt_volver = findViewById(R.id.bt_ter_volver)
        bt_volver.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {

        mostrarAviso(bt_volver, "Por favor, lea las cláusulas")
    }
}