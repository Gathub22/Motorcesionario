package com.example.motorc.adaptadores
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import androidx.core.content.ContextCompat.startActivity
import com.example.motorc.R
import com.example.motorc.lib.Android.Companion.cargarUsuario
import com.example.motorc.lib.Herramientas.Companion.adaptarAvatar
import com.example.motorc.modelos.Usuario
import com.example.motorc.usuarios.EditarUsuario


class AdaptadorListaUsuarios(val lista_usuarios: List<Usuario>, val bd_ref: DatabaseReference, actividad: AppCompatActivity):
    RecyclerView.Adapter<AdaptadorListaUsuarios.UsuarioViewHolder>(), Filterable{

    private lateinit var contexto: Context
    private var lista_filtrada=lista_usuarios
    private val cliente = cargarUsuario(actividad)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {

        // Seleccionamos la vista que se va utilizar para representar los datos de cada elemento de la lista
        contexto = parent.context
        val vista_item = LayoutInflater.from(contexto).inflate(R.layout.item_usuario,parent, false)

        return UsuarioViewHolder(vista_item) // Ejecuta otra función que devuelve un ViewHolder
    }

    // Esta es la clase que se devuelve y sobre la que operará el RecyclerView
    inner class UsuarioViewHolder(usuarioView: View): RecyclerView.ViewHolder(usuarioView){
        val nombre = usuarioView.findViewById<TextView>(R.id.tv_usr_nombre)
        val estado = usuarioView.findViewById<FrameLayout>(R.id.fr_usr_estado)
        val avatar = usuarioView.findViewById<ImageView>(R.id.iv_usr_avatar)
        val admin = usuarioView.findViewById<ImageView>(R.id.iv_usr_admin)
        val chat = usuarioView.findViewById<ImageView>(R.id.iv_usr_chat)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {

        val item_actual=lista_filtrada!![position]  // Sacamos el objeto de la lista

        holder.nombre.text = item_actual.nombre
        adaptarAvatar(contexto, item_actual.foto_url).into(holder.avatar)

        holder.avatar.setOnClickListener {
            val intencion = Intent(contexto, EditarUsuario::class.java)
            intencion.putExtra("revisar", item_actual)
            startActivity(contexto, intencion, null)
        }

        holder.chat.setOnClickListener {
            val intencion = Intent(contexto, EditarUsuario::class.java)
            intencion.putExtra("revisar", item_actual)
            startActivity(contexto, intencion, null)
        }
    }
    override fun getItemCount(): Int = lista_filtrada!!.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val busqueda = constraint.toString().lowercase()
                if (busqueda.length < 3) {
                    lista_filtrada = lista_usuarios
                } else {
                    lista_filtrada = (lista_usuarios.filter { // Se añaden los it (objetos candidatos) si cumplen
                        // una de las condiciones de abajo
                        it.nombre.toString().lowercase().contains(busqueda) ||
                                it.email.toString().contains(busqueda)
                    }) as MutableList<Usuario>
                }
                val filterResults = FilterResults()
                filterResults.values = lista_filtrada
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) = notifyDataSetChanged()

        }
    }
}