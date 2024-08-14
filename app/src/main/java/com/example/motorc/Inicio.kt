package com.example.motorc

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.motorc.inicio.Portada
import com.example.motorc.lib.Android.Companion.accederDatos
import com.example.motorc.lib.Android.Companion.cargarUsuario
import com.example.motorc.lib.Android.Companion.guardarUsuario
import com.example.motorc.lib.Android.Companion.mostrarAviso
import com.example.motorc.lib.Firebase.Companion.verUsuario
import com.example.motorc.lib.Herramientas.Companion.actualizarAppBar
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Usuario
import com.example.motorc.productos.ListaPedidos
import com.example.motorc.productos.ListaProductos
import com.example.motorc.usuarios.ListaUsuarios
import com.example.motorc.usuarios.Boxes
import com.example.motorc.usuarios.Ajustes
import com.example.motorc.usuarios.EditarUsuario

class Inicio : AppCompatActivity() {
    private lateinit var datos_app: SharedPreferences
    private lateinit var menu_avatar: ImageView
    private lateinit var menu_nombre: TextView
    private lateinit var menu_ajustes: ImageView
    private lateinit var menu_admin: ImageView

    private var usuario: Usuario? = null

    private lateinit var ll_boxes: LinearLayout
    private lateinit var ll_eventos: LinearLayout
    private lateinit var ll_usuarios: LinearLayout
    private lateinit var ll_pedidos: LinearLayout
    private lateinit var ll_catalogo: LinearLayout
    private lateinit var bt_salir: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_motor)

        usuario = cargarUsuario(this)
        verUsuario("id",usuario!!.id!!){
            usuario = it
        }

        if(usuario!!.email == null){
            log("Volviendo a la Portada...")
            Toast.makeText(applicationContext, "Por favor, inicia sesión de nuevo", Toast.LENGTH_SHORT).show()
            val intencion = Intent(applicationContext, Portada::class.java)
            intencion.putExtra("fallo", 1)
            startActivity(intencion)
        }else{

            datos_app = accederDatos(this)

            if(!usuario!!.disponible){
                log("Volviendo a la Portada...")
                Toast.makeText(applicationContext, "Tu cuenta ha sido cerrada", Toast.LENGTH_SHORT).show()
                datos_app.edit().putString("email", null).commit()
                val intencion = Intent(applicationContext, Portada::class.java)
                intencion.putExtra("fallo", 1)
                startActivity(intencion)
                finish()
            }else{
                guardarUsuario(this, usuario!!)
                cargarInicio()
            }
        }
    }

    fun cargarInicio(){
        // GUARDAR SESION
        datos_app.edit().putString("email", usuario!!.email).commit()
        log("Sesión guardada")

        menu_avatar = findViewById(R.id.iv_ipr_foto)
        menu_nombre = findViewById(R.id.tv_ipr_nombre)
        menu_ajustes = findViewById(R.id.iv_ipr_comprar)
        menu_admin = findViewById(R.id.iv_usr_admin)

        actualizarAppBar(applicationContext, usuario!!, menu_avatar, menu_nombre, menu_admin)

        menu_ajustes.setOnClickListener {
            val intencion = Intent(applicationContext, Ajustes::class.java)
            startActivity(intencion)
        }

        ll_boxes = findViewById<LinearLayout>(R.id.ll_ini_gateria)
        ll_boxes.setOnClickListener {
            val intencion = Intent(applicationContext, Boxes::class.java)
            startActivity(intencion)
        }

        ll_eventos = findViewById(R.id.ll_ini_eventos)
        ll_eventos.setOnClickListener {
            mostrarAviso(ll_eventos, "En construcción...")
        }

        ll_usuarios = findViewById(R.id.ll_ini_usuarios)
        if(usuario!!.admin!!){
            ll_usuarios.setOnClickListener {
                val intencion = Intent(applicationContext, ListaUsuarios::class.java)
                startActivity(intencion)
            }
        }else{
            ll_usuarios.visibility = View.GONE
        }

        ll_catalogo = findViewById(R.id.ll_ini_productos)
        ll_catalogo.setOnClickListener {
            val intencion = Intent(applicationContext, ListaProductos::class.java)
            startActivity(intencion)
        }

        ll_pedidos = findViewById(R.id.ll_ini_pedidos)
        ll_pedidos.setOnClickListener {
            val intencion = Intent(applicationContext, ListaPedidos::class.java)
            startActivity(intencion)
        }

        bt_salir = findViewById(R.id.bt_ini_salir)
        bt_salir.setOnClickListener {
            datos_app.edit().putString("id", null).commit()
            Toast.makeText(applicationContext, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            val intencion = Intent(applicationContext, Portada::class.java)
            startActivity(intencion)
            finish()
        }
    }

    override fun onBackPressed() {
        Toast.makeText(applicationContext, "Para salir pulsa \"Cerrar sesión\"", Toast.LENGTH_SHORT).show()
    }
}