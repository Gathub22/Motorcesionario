package com.example.motorc.usuarios

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motorc.R
import com.example.motorc.adaptadores.AdaptadorBoxes
import com.example.motorc.lib.Android.Companion.cargarUsuario
import com.example.motorc.lib.Herramientas.Companion.adaptarAvatar
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Mensaje
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class Boxes : AppCompatActivity(){
    var bd_ref = FirebaseDatabase.getInstance().getReference()
    private var lista_mensajes = mutableListOf<Mensaje>()
    private lateinit var recycler: RecyclerView
    private lateinit var adaptador: AdaptadorBoxes

    private lateinit var iv_logo: ImageView
    private lateinit var tv_titulo: TextView
    private lateinit var tv_desc: TextView
    private lateinit var et_entrada: EditText
    private lateinit var iv_enviar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vista_chat)

        iv_logo = findViewById(R.id.iv_ipr_foto)
        tv_titulo = findViewById(R.id.tv_ipr_nombre)
        tv_desc = findViewById(R.id.tv_ipr_precio)

        adaptarAvatar(applicationContext, R.drawable.boxes).into(iv_logo)
        tv_titulo.setText(R.string.boxes_titulo)
        tv_desc.setText(R.string.boxes_desc)

        val usuario = cargarUsuario(this)

        bd_ref.child("boxes").child("mensajes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista_mensajes.clear()
                    snapshot.children.forEach{ hijo:DataSnapshot?->
                        val pojo_mensaje=hijo?.getValue(Mensaje::class.java)
                        lista_mensajes.add(pojo_mensaje!!)
                    }
                    //Notificamos al adaptador del recycler de la vista que hay nuevos cambios
                    recycler.adapter?.notifyDataSetChanged()
                    recycler.scrollToPosition(lista_mensajes.size - 1)
                }

                override fun onCancelled(error: DatabaseError) = log("e", error.toString())
            })

        adaptador = AdaptadorBoxes(lista_mensajes, this, applicationContext)

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


                val id_pub = bd_ref.child("boxes").child("mensajes")
                    .push().key
                val publicacion = Mensaje(id_pub!!, usuario!!.id!!, usuario!!.foto_url!!, fecha_hora, mensaje)

                bd_ref.child("boxes").child("mensajes")
                    .child(id_pub!!)
                    .setValue(publicacion)

                et_entrada.text.clear()
            } else {
                Toast.makeText(applicationContext, "No has escrito nada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}