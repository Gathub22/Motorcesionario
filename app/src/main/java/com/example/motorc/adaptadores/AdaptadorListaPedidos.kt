package com.example.motorc.adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.motorc.R
import com.example.motorc.lib.Android.Companion.mostrarAviso
import com.example.motorc.lib.Firebase.Companion.enviarPedido
import com.example.motorc.lib.Firebase.Companion.verProducto
import com.example.motorc.lib.Firebase.Companion.verUsuario
import com.example.motorc.lib.Herramientas.Companion.adaptarAvatar
import com.example.motorc.modelos.Pedido
import com.example.motorc.modelos.Producto
import com.example.motorc.modelos.Usuario
import com.example.motorc.productos.ChatPedido
import com.example.motorc.productos.EditarProductos
import com.example.motorc.usuarios.EditarUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

class AdaptadorListaPedidos(actividad: AppCompatActivity, contextoApp: Context, val lista_pedidos: MutableList<Pedido>, val usuario: Usuario) :  RecyclerView.Adapter<AdaptadorListaPedidos.PedidoViewHolder>(){

    private var actividad = actividad
    private var contexto = contextoApp
    private var lista_filtrada = lista_pedidos

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        // Seleccionamos la vista que se va utilizar para representar los datos de cada elemento de la lista
        contexto = parent.context

        val vista_item = LayoutInflater.from(contexto).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(vista_item) // Ejecuta otra función que devuelve un ViewHolder
    }

    // Esta es la clase que se devuelve y sobre la que operará el RecyclerView
    inner class PedidoViewHolder(pedidoView: View): RecyclerView.ViewHolder(pedidoView){
        val prod_nombre = pedidoView.findViewById<TextView>(R.id.tv_ipd_prod)
        val prod_imagen = pedidoView.findViewById<ImageView>(R.id.iv_ipd_prod)
        val usr_nombre = pedidoView.findViewById<TextView>(R.id.tv_ipd_usr)
        val usr_imagen = pedidoView.findViewById<ImageView>(R.id.iv_ipd_usr)
        val tv_mensaje = pedidoView.findViewById<TextView>(R.id.tv_ipd_mensaje)
        val tv_estado = pedidoView.findViewById<TextView>(R.id.tv_ipd_estado)
        val bt_aceptar = pedidoView.findViewById<ImageView>(R.id.iv_ipd_aceptar)
        val bt_enviar = pedidoView.findViewById<ImageView>(R.id.iv_ipd_enviar)
        val bt_rechazar = pedidoView.findViewById<ImageView>(R.id.iv_ipd_rechazar)
        val bt_recibir = pedidoView.findViewById<ImageView>(R.id.iv_ipd_recibir)
        val bt_chat = pedidoView.findViewById<ImageView>(R.id.iv_ipd_chat)
    }


    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {

        val item_actual=lista_filtrada!![position]  // Sacamos el objeto de la lista

        holder.prod_nombre.text = item_actual.nom_prod
        Glide.with(actividad).load(item_actual.img_prod).into(holder.prod_imagen)
        holder.prod_imagen.setOnClickListener {

            GlobalScope.launch (Dispatchers.IO){

                val intencion = Intent(contexto, EditarProductos::class.java)

                val semaforo = CountDownLatch(1)
                var prod: Producto? = null
                verProducto("id",item_actual.id_prod){
                    prod = it
                    semaforo.countDown()
                }
                semaforo.await()
                intencion.putExtra("producto", prod)
                if(usuario.admin!!) intencion.putExtra("crear", 1)
                startActivity(contexto, intencion, null)
            }
        }


        holder.usr_nombre.text = item_actual.cliente_nom
        adaptarAvatar(contexto,item_actual.cliente_img).into(holder.usr_imagen)
        holder.usr_imagen.setOnClickListener {

            GlobalScope.launch (Dispatchers.IO){
                val s = CountDownLatch(1)
                val intencion = Intent(contexto, EditarUsuario::class.java)

                verUsuario("id",item_actual.cliente_id){
                    intencion.putExtra("revistar", it)
                    s.countDown()
                }
                s.await()
                startActivity(contexto, intencion, null)
            }

        }

        holder.tv_mensaje.text = item_actual.mensaje

        var estado = ""
        if(item_actual.estado == 0){
            estado = "PENDIENTE"
        }else if(item_actual.estado == 1){
            estado = "ACEPTADO"
            holder.bt_rechazar.visibility = View.GONE
            holder.bt_aceptar.visibility = View.GONE
        }else if(item_actual.estado == 2){
            estado = "ENVIADO"
            holder.bt_aceptar.visibility = View.GONE
            holder.bt_enviar.visibility = View.GONE
            holder.bt_rechazar.visibility = View.GONE
        }else if(item_actual.estado == 3){
            estado = "COMPLETADO"
            holder.bt_aceptar.visibility = View.GONE
            holder.bt_enviar.visibility = View.GONE
            holder.bt_recibir.visibility = View.GONE
            holder.bt_rechazar.visibility = View.GONE
        }else{
            estado = "RECHAZADO"
            holder.bt_enviar.visibility = View.GONE
            holder.bt_recibir.visibility = View.GONE
            holder.bt_aceptar.visibility = View.GONE
            holder.bt_rechazar.visibility = View.GONE
        }
        holder.tv_estado.text = estado

        if(!usuario.admin!!){
            holder.bt_aceptar.visibility = View.GONE
            holder.bt_enviar.visibility = View.GONE
            holder.bt_rechazar.visibility = View.GONE

            if(item_actual.estado != 2){
                holder.bt_recibir.visibility = View.GONE
            }
        }

        holder.bt_aceptar.setOnClickListener {
            val conds = arrayOf(1,2,3,4)
            if(item_actual.estado !in conds){
                item_actual.estado = 1
                enviarPedido(item_actual)

                holder.tv_estado.text = "ACEPTADO"
                holder.bt_rechazar.visibility = View.GONE
                mostrarAviso(holder.bt_aceptar, "Pedido aceptado")
            }
        }

        holder.bt_enviar.setOnClickListener {
            if(item_actual.estado == 1){
                item_actual.estado = 2
                enviarPedido(item_actual)

                holder.tv_estado.text = "ENVIADO"
                holder.bt_aceptar.visibility = View.GONE
                mostrarAviso(holder.bt_aceptar, "Pedido enviado")
            }
        }

        holder.bt_recibir.setOnClickListener {
            if(item_actual.estado == 2){
                item_actual.estado = 3
                enviarPedido(item_actual)

                holder.tv_estado.text = "COMPLETADO"
                holder.bt_aceptar.visibility = View.GONE

                mostrarAviso(holder.bt_aceptar, "Pedido recibido")
            }
        }

        holder.bt_rechazar.setOnClickListener {
            if(item_actual.estado < 3){
                item_actual.estado = 4
                enviarPedido(item_actual)

                holder.tv_estado.text = "RECHAZADO"
                holder.bt_aceptar.visibility = View.GONE
                holder.bt_enviar.visibility = View.GONE
                holder.bt_recibir.visibility = View.GONE
                holder.bt_rechazar.visibility = View.GONE
                mostrarAviso(holder.bt_aceptar, "Pedido rechazado")
            }
        }

        holder.bt_chat.setOnClickListener {
            val intencion = Intent(contexto, ChatPedido::class.java)
            intencion.putExtra("pedido", item_actual.id)
            intencion.putExtra("producto", item_actual.id_prod)
            startActivity(contexto, intencion, null)
        }

    }

    override fun getItemCount(): Int = lista_filtrada!!.size
}