package com.example.motorc.productos

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.motorc.R
import com.example.motorc.lib.Android.Companion.calcularPrecio
import com.example.motorc.lib.Android.Companion.cargarUsuario
import com.example.motorc.lib.Android.Companion.verMoneda
import com.example.motorc.lib.Firebase.Companion.enviarPedido
import com.example.motorc.lib.Herramientas.Companion.adaptarAvatar
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Pedido
import com.example.motorc.modelos.Producto
import com.example.motorc.modelos.Usuario
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class FormPedido : AppCompatActivity() {

    lateinit var iv_prod: ImageView
    lateinit var tv_prod: TextView
    lateinit var iv_usr: ImageView
    lateinit var tv_usr: TextView
    lateinit var tv_precio: TextView
    lateinit var et_mensaje: EditText
    lateinit var bt_comprar: Button

    lateinit var producto: Producto
    lateinit var usuario: Usuario
    val ref_bd = FirebaseDatabase.getInstance().getReference()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_pedido)

        usuario = cargarUsuario(this)!!
        producto = intent.getParcelableExtra<Producto>("producto")!!

        iv_prod = findViewById(R.id.iv_ped_prod)
        tv_prod = findViewById(R.id.tv_ped_prod)
        iv_usr = findViewById(R.id.iv_ped_usr)
        tv_usr = findViewById(R.id.tv_ped_usr)
        tv_precio = findViewById(R.id.tv_ped_precio)
        et_mensaje = findViewById(R.id.et_ped_mensaje)
        bt_comprar = findViewById(R.id.bt_ped_comprar)

        Glide.with(this).load(producto.foto).into(iv_prod)
        tv_prod.text = producto.nombre

        adaptarAvatar(this, usuario.foto_url).into(iv_usr)
        tv_usr.text = usuario.nombre

        val m = verMoneda(this)
        tv_precio.text = String.format("%.2f $m",calcularPrecio(m, producto.precio))

        bt_comprar.setOnClickListener {
            log("Creando pedido...")

            val calendario = Calendar.getInstance().time
            val formateador = SimpleDateFormat("YYYY-MM-dd")
            val hoy = formateador.format(calendario)
            val texto = "${et_mensaje.text.toString()}\n"

            val id = ref_bd.child("pedidos").push().key.toString()
            val pedido = Pedido(
                id,
                producto.foto,
                producto.id,
                producto.nombre,
                texto,
                0,
                usuario.id!!,
                usuario.nombre!!,
                usuario.foto_url!!
            )

            enviarPedido(pedido)

            val intencion = Intent(applicationContext, ListaPedidos::class.java)
            startActivity(intencion)
            finish()
        }

    }

    override fun onBackPressed() {

        val intencion = Intent(applicationContext, EditarProductos::class.java)
        intencion.putExtra("producto", producto)
        startActivity(intencion)
        finish()
    }
}
