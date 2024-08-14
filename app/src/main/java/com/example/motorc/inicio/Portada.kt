package com.example.motorc.inicio

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.motorc.Inicio
import com.example.motorc.R
import com.example.motorc.lib.Android.Companion.accederDatos
import com.example.motorc.lib.Android.Companion.guardarUsuario
import com.example.motorc.lib.Android.Companion.mostrarAviso
import com.example.motorc.lib.Firebase.Companion.verUsuario
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Usuario
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

class Portada : AppCompatActivity(){

    lateinit var datos_app: SharedPreferences
    lateinit var ref_bd: DatabaseReference

    lateinit var bt_registro: Button
    lateinit var tv_ingreso: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.portada)

        if(intent.getIntExtra("fallo",0) != 1){
            datos_app = accederDatos(this)

            val modo_noche = datos_app.getBoolean("noche", false)
            if(modo_noche){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                log("n","MODO NOCHE ACTIVADO")
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                log("n","MODO DIA ACTIVADO")
            }

            val id = datos_app.getString("id", null)

            if(id != null){
                GlobalScope.launch(Dispatchers.IO){
                    log("n","USUARIO DETECTADO (${id})")

                    val semaforo = CountDownLatch(1)
                    ref_bd = FirebaseDatabase.getInstance().getReference()

                    var usuario: Usuario? = null
                    verUsuario("id",id){
                        usuario = it
                        semaforo.countDown()
                    }
                    semaforo.await()
                    if(usuario != null){
                        guardarUsuario(this@Portada, usuario!!)
                        val intencion = Intent(applicationContext, Inicio::class.java)
                        startActivity(intencion)
                    }else{
                        println(usuario)
                        mostrarAviso(findViewById(R.id.bt_registro), "Autoinicio fallido. Inicie sesión")
                    }
                }
            }else{
                log("a", "No se ha detectado ningún usuario")
            }
        }

        bt_registro = findViewById(R.id.bt_registro)
        bt_registro.setOnClickListener {
            var intent = Intent(applicationContext, RegistroUsuario::class.java)
            startActivity(intent)
        }

        tv_ingreso = findViewById(R.id.tv_ing_ingreso)
        tv_ingreso.setOnClickListener {
            val intent = Intent(applicationContext, IngresoUsuario::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finishAffinity()
    }
}