package com.example.motorc.inicio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.motorc.Inicio
import com.example.motorc.R
import com.example.motorc.lib.Android.Companion.guardarUsuario
import com.example.motorc.lib.Android.Companion.mostrarAviso
import com.example.motorc.lib.Firebase.Companion.enviarUsuario
import com.example.motorc.lib.Firebase.Companion.guardarFoto
import com.example.motorc.lib.Firebase.Companion.verUsuario
import com.example.motorc.lib.Herramientas.Companion.adaptarEmail
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Usuario
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.CountDownLatch

class RegistroUsuario: AppCompatActivity() {
    lateinit var iv_foto: ImageView
    private var archivo_foto: Uri? = null

    lateinit var et_nombre: TextInputEditText
    lateinit var et_email: TextInputEditText
    lateinit var et_pass1: TextInputEditText
    lateinit var et_pass2: TextInputEditText
    lateinit var et_dir: TextInputEditText
    lateinit var sp_prod: Spinner
    lateinit var sp_estado: Spinner
    lateinit var tv_terminos: TextView
    lateinit var cb_terminos: CheckBox
    lateinit var bt_registro: Button

    lateinit var ref_bd : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_registro)

        ref_bd = FirebaseDatabase.getInstance().reference

        et_nombre = findViewById(R.id.et_reg_nombre)
        et_email = findViewById(R.id.et_reg_email)
        et_pass1 = findViewById(R.id.et_reg_pass1)
        et_pass2 = findViewById(R.id.et_reg_pass2)
        et_dir = findViewById(R.id.et_reg_direccion)
        cb_terminos = findViewById(R.id.cb_reg_terminos)

        iv_foto = findViewById(R.id.iv_reg_foto)
        iv_foto.setOnClickListener {
            val ficheroTemp = crearFicheroImagen()
            archivo_foto = FileProvider.getUriForFile(
                applicationContext,
                "com.example.motorc.fileprovider",
                ficheroTemp
            )
            getCamara.launch(archivo_foto)
        }
        iv_foto.setOnLongClickListener {
            getGaleria.launch("image/*")
            true
        }

        tv_terminos = findViewById(R.id.tv_reg_terminos)
        tv_terminos.setOnClickListener {
            val intencion = Intent(applicationContext, Terminos::class.java)
            startActivity(intencion)
        }

        bt_registro = findViewById(R.id.bt_reg_registro)
        bt_registro.setOnClickListener {
            var valido = true

            val nombre = et_nombre.text.toString()
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

            var email = et_email.text.toString()
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                et_email.error = resources.getString(R.string.reg_sin_email)
                valido = false
            }else{
                email = adaptarEmail(email)
            }

            val pass1 = et_pass1.text.toString().trim()
            if(pass1.length < 8){
                et_pass1.error = resources.getString(R.string.reg_sin_pass1)
                valido = false
            }else{
                val pass2 = et_pass2.text.toString()
                if(pass2 != pass1){
                    et_pass2.error = resources.getString(R.string.reg_desiguales)
                    valido = false
                }
            }

            if(!cb_terminos.isChecked){
               mostrarAviso(cb_terminos, resources.getString(R.string.reg_cb_terminos))
                valido = false
            }

            GlobalScope.launch (Dispatchers.IO){
                var semaforo = CountDownLatch(1)


                if(valido){
                    var usuario: Usuario? = null
                    verUsuario("email", email){
                        usuario = it
                        semaforo.countDown()
                    }
                    semaforo.await()
                    if(usuario != null){
                        log("Encontrado un usuario con el mismo email en Firebase")
                        valido = false
                        mostrarAviso(cb_terminos, "Ya existe un usuario con ese email")
                    }else{
                        log("No se ha encontrado ningun usuario con ese email en Firebase")
                    }

                    if(valido){
                        semaforo = CountDownLatch(1)

                        verUsuario("nombre", nombre){
                            usuario = it
                            semaforo.countDown()
                        }
                        semaforo.await()
                        if(usuario != null){
                            log("Encontrado un usuario con el mismo email en Firebase")
                            valido = false
                            mostrarAviso(cb_terminos, "Ya existe un usuario con ese email")
                        }else{
                            log("No se ha encontrado ningun usuario con ese email en Firebase")
                        }
                    }

                }

                if(valido){
                    log("Creando nuevo usuario...")
                    val actividad = this
                    GlobalScope.launch (Dispatchers.IO) {
                        val direccion = et_dir.text.toString().trim()
                        val id = ref_bd.push().key

                        val calendario = Calendar.getInstance().time
                        val formateador = SimpleDateFormat("YYYY-MM-dd")
                        val hoy = formateador.format(calendario)

                        var dir_foto = ""
                        if (archivo_foto != null) dir_foto = guardarFoto("usuarios", archivo_foto)

                        val usuario = Usuario(id, email, nombre, dir_foto, direccion, false, hoy, pass1, true)
                        enviarUsuario(usuario)

                        guardarUsuario(this@RegistroUsuario, usuario)

                        log("n","Entrando al inicio ...")
                        val intencion = Intent(applicationContext, Inicio::class.java)
                        startActivity(intencion)
                    }
                }else{
                    log("Usuario introducido no válido. Cancelado")
                }
            }

        }
    }

    val getCamara=registerForActivityResult(ActivityResultContracts.TakePicture()){

        if(it){
            iv_foto.setImageURI(archivo_foto)
        }else{
            Snackbar.make(iv_foto,
                R.string.sinfoto,
                Snackbar.LENGTH_LONG).show()
        }
    }

    val getGaleria=registerForActivityResult(ActivityResultContracts.GetContent()){

        // it es un valor que acompaña a la función. Si esa función no guarda ningún valor, que avise
        if(it==null){
            mostrarAviso(iv_foto, resources.getString(R.string.sinfoto))
        }else{
            // Si el it tiene una ruta de un archivo (el URI), que lo cambie en el ImageView
            archivo_foto = it
            iv_foto.setImageURI(it)
        }
    }

    private fun crearFicheroImagen(): File {
        val cal:Calendar?= Calendar.getInstance()

        val timeStamp:String?= SimpleDateFormat("yyyyMMdd_HHmmss").format(cal!!.time)
        val nombreFichero:String?="JPEG_"+timeStamp+"_"
        val carpetaDir: File?=applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val ficheroImagen: File?= File.createTempFile(nombreFichero!!,".jpg",carpetaDir)

        return ficheroImagen!!
    }
}
