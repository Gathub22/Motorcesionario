package com.example.motorc.usuarios

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.example.motorc.R
import com.example.motorc.inicio.Portada
import com.example.motorc.lib.Android.Companion.accederDatos
import com.example.motorc.lib.Android.Companion.cargarUsuario
import com.example.motorc.lib.Android.Companion.guardarMoneda
import com.example.motorc.lib.Android.Companion.mostrarAviso
import com.example.motorc.lib.Android.Companion.verMoneda
import com.example.motorc.lib.Herramientas.Companion.adaptarAvatar
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Usuario
import com.example.motorc.usuarios.EditarUsuario

class Ajustes : AppCompatActivity() {

    private lateinit var sw_noche: Switch
    private lateinit var iv_foto: ImageView
    private lateinit var sp_monedas: Spinner

    private lateinit var usuario: Usuario

    private var monedaCambiada = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ajustes)
        usuario = cargarUsuario(this)!!

        iv_foto = findViewById(R.id.iv_aju_avatar)
        adaptarAvatar(applicationContext, usuario.foto_url).into(iv_foto)
        iv_foto.setOnClickListener {
            val intencion = Intent(applicationContext, EditarUsuario::class.java)
            intencion.putExtra("revisar", usuario)
            startActivity(intencion)
        }

        sw_noche = findViewById(R.id.sw_aju_noche)

        val datos = accederDatos(this)
        sw_noche.isChecked = datos.getBoolean("noche", false)

        sw_noche.setOnCheckedChangeListener { compoundButton, checkeado ->

            if(checkeado){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                datos.edit().putBoolean("noche", true).commit()
                println("MODO NOCHE ACTIVADO")
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                datos.edit().putBoolean("noche", false).commit()
                println("MODO DIA ACTIVADO")
            }
        }

        sp_monedas = findViewById(R.id.sp_aju_moneda)
        // ESTADOS
        val lista_monedas = listOf<String>(
            "Euro" ,
            "Dólar americano",
            "Yen" ,
            "Rublo ruso" ,
            "Libra esterlina"
        )
        val lista_monedas_nom = hashMapOf(
            0 to "€",
            1 to "$",
            2 to "¥",
            3 to "₽",
            4 to "£"
        )



        val adaptador = ArrayAdapter(applicationContext,
            android.R.layout.simple_spinner_item,
            lista_monedas)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_monedas.adapter = adaptador
        sp_monedas.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, posicion: Int, p3: Long) {
                if(monedaCambiada){
                    guardarMoneda(this@Ajustes, lista_monedas_nom.get(posicion)!!)
                    mostrarAviso(sp_monedas, "Moneda cambiada a ${lista_monedas[posicion]}")
                }else{
                    log("Evitado cambio accidental")
                    monedaCambiada = true
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val m = verMoneda(this)
        if      (m == "€") sp_monedas.setSelection(0)
        else if (m == "\$") sp_monedas.setSelection(1)
        else if (m == "¥") sp_monedas.setSelection(2)
        else if (m == "₽") sp_monedas.setSelection(3)
        else if (m == "£") sp_monedas.setSelection(4)


    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
    }
}