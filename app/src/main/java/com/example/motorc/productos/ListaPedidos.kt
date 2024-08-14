package com.example.motorc.productos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.motorc.R
import com.example.motorc.adaptadores.AdaptadorListaPedidos
import com.example.motorc.adaptadores.AdaptadorListaProductos
import com.example.motorc.lib.Android.Companion.cargarUsuario
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Pedido
import com.example.motorc.modelos.Producto
import com.example.motorc.modelos.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListaPedidos: AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adaptador: AdaptadorListaPedidos

    lateinit var usuario: Usuario
    val ref_bd = FirebaseDatabase.getInstance().getReference()
    private var lista_pedidos: MutableList<Pedido> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_pedidos)

        usuario = cargarUsuario(this)!!
        adaptador = AdaptadorListaPedidos(this, applicationContext, lista_pedidos, usuario)

        recycler = findViewById(R.id.rcy_pedidos)
        recycler.adapter = adaptador

        if(usuario.admin!!){
            log("Mostrando pedidos como admin...")

            ref_bd.child("pedidos")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        lista_pedidos.clear()
                        snapshot.children.forEach{ hijo: DataSnapshot?->
                            val pojo_pedido=hijo?.getValue(Pedido::class.java)!!
                            lista_pedidos.add(pojo_pedido)
                        }
                        lista_pedidos.sortBy{
                            it.estado // TODO: MIRAR SI ESTA BIEN ORDENADO
                        }
                        //Notificamos al adaptador del recycler de la vista que hay nuevos cambios
                        recycler.adapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) = println(error.message)
                })
            
        }else{
            log("Mostrando pedidos del cliente...")

            ref_bd.child("pedidos")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        lista_pedidos.clear()
                        snapshot.children.forEach{ hijo: DataSnapshot?->
                            val pojo_pedido=hijo?.getValue(Pedido::class.java)!!
                            if(pojo_pedido.cliente_id == usuario.id) lista_pedidos.add(pojo_pedido)
                        }

                        lista_pedidos.sortBy{
                            it.estado // TODO: MIRAR SI ESTA BIEN ORDENADO
                        }
                        //Notificamos al adaptador del recycler de la vista que hay nuevos cambios
                        recycler.adapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) = println(error.message)
                })
        }

    }
}