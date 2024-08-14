package com.example.motorc.usuarios

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.motorc.R
import com.example.motorc.inicio.Portada
import com.example.motorc.lib.Android.Companion.accederDatos
import com.example.motorc.lib.Android.Companion.cargarUsuario
import com.example.motorc.lib.Android.Companion.guardarUsuario
import com.example.motorc.lib.Android.Companion.mostrarAviso
import com.example.motorc.lib.Firebase.Companion.enviarUsuario
import com.example.motorc.lib.Firebase.Companion.guardarFoto
import com.example.motorc.lib.Firebase.Companion.verUsuario
import com.example.motorc.lib.Herramientas.Companion.adaptarAvatar
import com.example.motorc.lib.Herramientas.Companion.adaptarEmail
import com.example.motorc.lib.Herramientas.Companion.humanizarEmail
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Usuario
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch

class EditarUsuario : AppCompatActivity(){

    private lateinit var fr_estado_avatar: FrameLayout
    private lateinit var iv_avatar: ImageView
    private lateinit var et_nombre: EditText
    private lateinit var et_email: EditText
    private lateinit var et_direccion: EditText
    private lateinit var tv_fecha: TextView
    private lateinit var ll_mods: LinearLayout
    private lateinit var et_pass: EditText
    private lateinit var bt_disponible: Button
    private lateinit var bt_aplicar: Button
    private lateinit var iv_reiniciar: ImageView
    private lateinit var et_ubicacion: EditText

    private lateinit var usuarioOriginal: Usuario
    private lateinit var cliente: Usuario

    private val ref_bd = FirebaseDatabase.getInstance().getReference()

    private var prod: Int = 0
    private var estado: Int = 0
    private var esAdmin: Boolean? = null
    private var vive: Boolean = true
    private var dir_foto: Uri? = null

    var foto_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_usuario)

        usuarioOriginal = intent.getParcelableExtra("revisar")!!


            cliente = cargarUsuario(this@EditarUsuario)!!

            fr_estado_avatar = findViewById(R.id.fr_ver_estado_avatar)
            iv_avatar = findViewById(R.id.iv_ver_avatar)
            et_nombre = findViewById(R.id.et_ver_nombre)
            et_email = findViewById(R.id.et_ver_email)
            tv_fecha = findViewById(R.id.tv_ver_fecha)
            ll_mods = findViewById(R.id.ll_ver_mods)
            bt_disponible = findViewById(R.id.bt_ver_disponible)
            bt_aplicar = findViewById(R.id.bt_ver_aplicar)
            iv_reiniciar = findViewById(R.id.iv_ver_reiniciar)
            et_ubicacion = findViewById(R.id.et_ver_ubicacion)
            et_pass = findViewById(R.id.et_ver_pass)
            et_direccion = findViewById(R.id.et_ver_ubicacion)

            vive = usuarioOriginal.disponible


            if(cliente.id == usuarioOriginal.id && cliente.admin!!){
                bt_disponible.visibility = View.GONE
            }


            if(vive) {
                prepararBloquear()
            }
            else{
                prepararRecuperar()
            }

            iv_reiniciar.setOnClickListener {
                Toast.makeText(applicationContext, "Reiniciando...", Toast.LENGTH_SHORT).show()
                cargarViews()
            }


            cargarViews()
            bt_aplicar.setOnClickListener {

                var valido = true

                var email = et_email.text.toString().trim()
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    log("Emáil introducido no válido")
                    valido = false
                    et_email.error = "Emáil no válido"
                }else{
                    email = adaptarEmail(email)

                    verUsuario("email",email){
                        if(it != null){
                            if(it.email == email && it.id != usuarioOriginal.id){
                                valido = false
                                et_email.error = "Ya existe un usuario con ese emáil"
                            }
                        }
                    }
                }

                var nombre = et_nombre.text.toString().trim()
                if(nombre.length < 5){
                    et_nombre.error = resources.getString(R.string.reg_sin_nom)
                    valido = false
                }else{
                    verUsuario("nombre", nombre) {
                        if (it != null)
                            if (it.disponible) {
                                valido = false
                                log("Usuario con el mismo nombre encontrado")
                                et_nombre.error = resources.getString(R.string.reg_sin_nom2)
                            }
                    }
                }

                val pass = et_pass.text.toString().trim()
                if(pass.length < 8){
                    et_pass.error = resources.getString(R.string.reg_sin_pass1)
                    valido = false
                }

                var foto_url = ""
                if(valido) {
                    GlobalScope.launch (Dispatchers.IO){

                        if(foto_uri != null){
                            foto_url = guardarFoto("usuarios", foto_uri)
                            println(foto_url)
                        }else{
                            foto_url = usuarioOriginal.foto_url!!
                        }
                        val usuario = Usuario(
                            id = usuarioOriginal.id,
                            email = email,
                            nombre = nombre,
                            contrasena = pass,
                            foto_url = foto_url,
                            direccion = et_direccion.text.toString().trim(),
                            admin = usuarioOriginal.admin,
                            fecha_creacion = usuarioOriginal.fecha_creacion,
                            disponible = usuarioOriginal.disponible,
                        )

                        enviarUsuario(usuario)

                        if(usuario.id == cliente.id){
                            guardarUsuario(this@EditarUsuario, usuario)
                        }
                        mostrarAviso(et_nombre, "Datos guardados")
                        log("Usuario enviado a Firebase")
                    }
                }else log("Datos rechazados para enviar")
            }

            iv_avatar.setOnClickListener {
                val ficheroTemp = crearFicheroImagen()
                foto_uri = FileProvider.getUriForFile(
                    applicationContext,
                    "com.example.motorc.fileprovider",
                    ficheroTemp
                )
                getCamara.launch(foto_uri)
            }

            iv_avatar.setOnLongClickListener {
                getGaleria.launch("image/*")
                true
            }

            runOnUiThread {
                adaptarAvatar(applicationContext, usuarioOriginal.foto_url).into(iv_avatar)
            }

    }
    fun prepararBloquear(){
        bt_disponible.setText("Bloquear")
        bt_disponible.setOnClickListener {
            usuarioOriginal.disponible = false
            enviarUsuario(usuarioOriginal)
            mostrarAviso(bt_disponible, "Usuario bloqueado")
            if(usuarioOriginal.id != cliente.id){
                prepararRecuperar()
            }else{
                val u = usuarioOriginal
                enviarUsuario(u)
                accederDatos(this).edit().putString("id", null).commit()
                Toast.makeText(applicationContext, "Sesión cerrada", Toast.LENGTH_SHORT).show()

                val intencion = Intent(applicationContext, Portada::class.java)
                startActivity(intencion)
                finish()
            }
        }
    }

    fun prepararRecuperar(){
        bt_disponible.setText("Recuperar")
        bt_disponible.setOnClickListener {
            usuarioOriginal.disponible = true
            enviarUsuario(usuarioOriginal)
            mostrarAviso(bt_disponible, "Usuario recuperado")
            prepararBloquear()
        }
    }
    val getCamara=registerForActivityResult(ActivityResultContracts.TakePicture()){

        if(it){
            iv_avatar.setImageURI(foto_uri)
        }else{
            mostrarAviso(iv_avatar, "No has hecho ninguna foto")
        }
    }

    val getGaleria=registerForActivityResult(ActivityResultContracts.GetContent()){

        // it es un valor que acompaña a la función. Si esa función no guarda ningún valor, que avise
        if(it==null){
            mostrarAviso(bt_disponible, "No has hecho ninguna foto")
        }else{
            // Si el it tiene una ruta de un archivo (el URI), que lo cambie en el ImageView
            foto_uri = it
            iv_avatar.setImageURI(it)
        }
    }




    private fun cargarViews() {
        log("Cargando vistas")

        et_nombre.setText(usuarioOriginal.nombre)
        adaptarAvatar(applicationContext, usuarioOriginal.foto_url)
        et_email.setText(humanizarEmail(usuarioOriginal.email!!))
        tv_fecha.setText("Se unió el ${usuarioOriginal.fecha_creacion}")
        et_ubicacion.setText(usuarioOriginal.direccion)
        et_pass.setText(usuarioOriginal.contrasena)
    }

    private fun crearFicheroImagen(): File {
        val cal: Calendar?= Calendar.getInstance()

        val timeStamp:String?= SimpleDateFormat("yyyyMMdd_HHmmss").format(cal!!.time)
        val nombreFichero:String?="JPEG_"+timeStamp+"_"
        val carpetaDir: File?=applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val ficheroImagen: File?= File.createTempFile(nombreFichero!!,".jpg",carpetaDir)

        return ficheroImagen!!
    }
}

