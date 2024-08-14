package com.example.motorc.inicio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.motorc.Inicio
import com.example.motorc.R
import com.example.motorc.lib.Android.Companion.guardarUsuario
import com.example.motorc.lib.Android.Companion.mostrarAviso
import com.example.motorc.lib.Firebase
import com.example.motorc.lib.Firebase.Companion.verUsuario
import com.example.motorc.lib.Herramientas.Companion.adaptarEmail
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Usuario
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch

class IngresoUsuario: AppCompatActivity() {

    private lateinit var et_email: EditText
    private lateinit var et_pass: EditText
    private lateinit var boton_ingreso: Button
    private lateinit var tv_registro: TextView

    private lateinit var bd_ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_ingreso)

        bd_ref = FirebaseDatabase.getInstance().getReference()

        et_email = findViewById(R.id.et_ing_email)
        et_pass = findViewById(R.id.et_ing_pass)

        boton_ingreso = findViewById<Button>(R.id.bt_ing_entrar)
        boton_ingreso.setOnClickListener {

            var valido = true

            var email = et_email.text.toString().trim()
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                valido = false
                mostrarAviso(boton_ingreso, "Emáil no válido")
            }else{
                email = adaptarEmail(email)
            }

            if(valido){
                val pass = et_pass.text.toString()

                GlobalScope.launch (Dispatchers.IO){
                    val semaforo = CountDownLatch(1)

                    var usuario: Usuario? = null
                    verUsuario("email", email) {
                        usuario = it
                        semaforo.countDown()
                    }
                    semaforo.await()
                    if(usuario != null){
                        log("Encontrado un usuario con el mismo email en Firebase")

                        if(usuario!!.disponible){
                            if(usuario!!.contrasena == pass){
                                guardarUsuario(this@IngresoUsuario, usuario!!)
                                val intencion = Intent(applicationContext, Inicio::class.java)
                                startActivity(intencion)
                            }else{
                                mostrarAviso(boton_ingreso, "Contraseña incorrecta")
                            }
                        }else{
                            mostrarAviso(boton_ingreso, "El usuario ha sido bloqueado")
                        }
                    }else{
                        valido = false
                        mostrarAviso(boton_ingreso, "No se ha encontrado ningún usuario")
                        log("No se ha encontrado ningun usuario con ese email en Firebase")
                    }
                }
            }else{
                log("Usuario introducido no válido. Cancelado")
            }
        }

        tv_registro = findViewById(R.id.tv_ing_ingreso)
        tv_registro.setOnClickListener {
            val intencion = Intent(applicationContext, RegistroUsuario::class.java)
            startActivity(intencion)
            finish()
        }
    }
}
