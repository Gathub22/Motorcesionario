package com.example.motorc.productos

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.motorc.R
import com.example.motorc.adaptadores.AdaptadorChatPedido
import com.example.motorc.lib.Android.Companion.calcularPrecio
import com.example.motorc.lib.Android.Companion.cargarUsuario
import com.example.motorc.lib.Android.Companion.verMoneda
import com.example.motorc.lib.Firebase.Companion.verProducto
import com.example.motorc.lib.Herramientas.Companion.adaptarAvatar
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.MensajePedido
import com.example.motorc.modelos.Producto
import com.example.motorc.modelos.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch

class ChatPedido : AppCompatActivity(){
    var bd_ref = FirebaseDatabase.getInstance().getReference()
    private var lista_mensajes = mutableListOf<MensajePedido>()
    private lateinit var recycler: RecyclerView
    private lateinit var adaptador: AdaptadorChatPedido

    private lateinit var iv_logo: ImageView
    private lateinit var tv_titulo: TextView
    private lateinit var tv_desc: TextView
    private lateinit var et_entrada: EditText
    private lateinit var iv_enviar: ImageView

    private lateinit var id_pedido: String
    private lateinit var usuario: Usuario
    private lateinit var producto: Producto

    override fun onCreate(savedInstanceState: Bundle?) {
        log("Iniciando chat de pedido...")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.vista_chat)

        usuario = cargarUsuario(this@ChatPedido)!!
        id_pedido = intent.getStringExtra("pedido")!!

        iv_logo = findViewById(R.id.iv_ipr_foto)
        tv_titulo = findViewById(R.id.tv_ipr_nombre)
        tv_desc = findViewById(R.id.tv_ipr_precio)

        log("Buscando producto...")
        val id_prod = intent.getStringExtra("producto")!!




        GlobalScope.launch(Dispatchers.IO) {

            val s = CountDownLatch(1)
            verProducto("id",id_prod) {
                producto = it!!
                s.countDown()
            }
            s.await()


            bd_ref.child("chat_pedidos").child("mensajes").child(id_pedido)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        lista_mensajes.clear()
                        snapshot.children.forEach { hijo: DataSnapshot? ->
                            val pojo_mensaje = hijo?.getValue(MensajePedido::class.java)
                            lista_mensajes.add(pojo_mensaje!!)
                        }
                        //Notificamos al adaptador del recycler de la vista que hay nuevos cambios
                        recycler.adapter?.notifyDataSetChanged()
                        recycler.scrollToPosition(lista_mensajes.size - 1)
                    }

                    override fun onCancelled(error: DatabaseError) = log("e", error.toString())
                })

            adaptador = AdaptadorChatPedido(lista_mensajes, this@ChatPedido, applicationContext)


            runOnUiThread {
                adaptarAvatar(applicationContext, producto!!.foto).into(iv_logo)
                iv_logo.setOnClickListener {
                    val intencion = Intent(applicationContext, EditarProductos::class.java)
                    intencion.putExtra("producto", producto)
                    if (usuario.admin!!) intencion.putExtra("crear", 1)
                    startActivity(intencion)
                }

                recycler = findViewById(R.id.rcy_chat)
                recycler.adapter = adaptador
                recycler.layoutManager = LinearLayoutManager(applicationContext)
                recycler.setHasFixedSize(true)
                recycler.recycledViewPool.setMaxRecycledViews(0, 0)

                et_entrada = findViewById(R.id.et_cht_entrada)
                iv_enviar = findViewById(R.id.iv_cht_enviar)
                iv_enviar.setOnClickListener() {

                    val mensaje = et_entrada.text.toString().trim()
                    if (mensaje != "") {

                        val ahora = Calendar.getInstance()
                        val formateador = SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
                        val fecha_hora = formateador.format(ahora.time)


                        val id_pub = bd_ref.child("chat_pedidos").child("mensajes").child(id_pedido)
                            .push().key
                        val publicacion = MensajePedido(
                            id_pub!!,
                            id_pedido,
                            usuario!!.id!!,
                            usuario!!.foto_url!!,
                            fecha_hora,
                            mensaje
                        )

                        bd_ref.child("chat_pedidos").child("mensajes").child(id_pedido)
                            .child(id_pub!!)
                            .setValue(publicacion)

                        et_entrada.text.clear()
                    } else {
                        Toast.makeText(applicationContext, "No has escrito nada", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            }

            runOnUiThread {
                tv_titulo.setText(producto!!.nombre)
                val m = verMoneda(this@ChatPedido)
                tv_desc.setText(String.format("%.2f $m", calcularPrecio(m, producto!!.precio)))
            }
        }
    }
}