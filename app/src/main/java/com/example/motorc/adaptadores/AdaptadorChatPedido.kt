package com.example.motorc.adaptadores
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat.startActivity
import com.example.motorc.R
import com.example.motorc.lib.Android.Companion.cargarUsuario
import com.example.motorc.lib.Firebase.Companion.verUsuario
import com.example.motorc.lib.Herramientas.Companion.adaptarAvatar
import com.example.motorc.modelos.Mensaje
import com.example.motorc.modelos.MensajePedido
import com.example.motorc.usuarios.EditarUsuario
class AdaptadorChatPedido(val lista_mensajes: List<MensajePedido>, actividad: AppCompatActivity, contexto: Context): RecyclerView.Adapter<AdaptadorChatPedido.BocadilloViewHolder>(){

    private lateinit var contexto: Context
    private val cliente_id = cargarUsuario(actividad)!!.id

    data class Participante (
        var ly: LinearLayout,
        var avatar: ImageView,
        var fecha: TextView,
        var mensaje: TextView
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BocadilloViewHolder {

        // Seleccionamos la vista que se va utilizar para representar los datos de cada elemento de la lista
        contexto = parent.context
        val vista_item = LayoutInflater.from(contexto).inflate(R.layout.bocadillo_chat,parent, false)

        return BocadilloViewHolder(vista_item) // Ejecuta otra función que devuelve un ViewHolder
    }

    // Esta es la clase que se devuelve y sobre la que operará el RecyclerView
    inner class BocadilloViewHolder(bocadilloView: View): RecyclerView.ViewHolder(bocadilloView){

        val items_emisor = Participante(
            ly = bocadilloView.findViewById<LinearLayout>(R.id.ly_cht_emisor),
            avatar = bocadilloView.findViewById<ImageView>(R.id.iv_cht_avatar_emisor),
            fecha = bocadilloView.findViewById<TextView>(R.id.tv_cht_fecha_emisor),
            mensaje = bocadilloView.findViewById<TextView>(R.id.tv_cht_mensaje_emisor)
        )

        val items_receptor = Participante(
            ly = bocadilloView.findViewById<LinearLayout>(R.id.ly_cht_receptor),
            avatar = bocadilloView.findViewById<ImageView>(R.id.iv_cht_avatar_receptor),
            fecha = bocadilloView.findViewById<TextView>(R.id.tv_cht_fecha_receptor),
            mensaje = bocadilloView.findViewById<TextView>(R.id.tv_cht_mensaje_receptor)
        )
    }

    override fun onBindViewHolder(holder: BocadilloViewHolder, position: Int) {

        val item_actual=lista_mensajes!![position]  // Sacamos el objeto de la lista

        if(item_actual.emisor_id == cliente_id){

            adaptarAvatar(contexto, item_actual.emisor_foto).into(holder.items_receptor.avatar)
            holder.items_receptor.fecha.text = item_actual.fecha
            holder.items_receptor.mensaje.text = item_actual.contenido

            holder.items_emisor.ly.visibility = View.GONE
            holder.items_emisor.avatar.visibility = View.GONE
            holder.items_emisor.fecha.visibility = View.GONE
            holder.items_emisor.mensaje.visibility = View.GONE

        }else{

            holder.items_emisor.fecha.text = item_actual.fecha
            holder.items_emisor.mensaje.text = item_actual.contenido
            adaptarAvatar(contexto, item_actual.emisor_foto).into(holder.items_emisor.avatar)
            
            holder.items_receptor.ly.visibility = View.GONE
            holder.items_receptor.avatar.visibility = View.GONE
            holder.items_receptor.fecha.visibility = View.GONE
            holder.items_receptor.mensaje.visibility = View.GONE
            
            
        }
    }

    override fun getItemCount(): Int = lista_mensajes!!.size
}