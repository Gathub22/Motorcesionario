package com.example.motorc.adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.motorc.R
import com.example.motorc.lib.Android.Companion.calcularPrecio
import com.example.motorc.lib.Android.Companion.verMoneda
import com.example.motorc.modelos.Producto
import com.example.motorc.modelos.Usuario
import com.example.motorc.productos.EditarProductos

class AdaptadorListaProductos(actividad: AppCompatActivity, contextoApp: Context, val lista_productos: List<Producto>, val usuario: Usuario) :  RecyclerView.Adapter<AdaptadorListaProductos.ProductoViewHolder>(),
    Filterable {

        private var monedaSimbolo = verMoneda(actividad)
        private var contexto = contextoApp
        private var lista_filtrada = lista_productos

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
            // Seleccionamos la vista que se va utilizar para representar los datos de cada elemento de la lista
            contexto = parent.context

            val vista_item = LayoutInflater.from(contexto).inflate(R.layout.item_producto, parent, false)
            return ProductoViewHolder(vista_item) // Ejecuta otra función que devuelve un ViewHolder
        }

        // Esta es la clase que se devuelve y sobre la que operará el RecyclerView
        inner class ProductoViewHolder(productoView: View): RecyclerView.ViewHolder(productoView){
            val nombre = productoView.findViewById<TextView>(R.id.tv_ipr_nombre)
            val precio = productoView.findViewById<TextView>(R.id.tv_ipr_precio)
            val imagen = productoView.findViewById<ImageView>(R.id.iv_ipr_foto)
            val comprar = productoView.findViewById<ImageView>(R.id.iv_ipr_comprar)
        }

        override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {

            val item_actual=lista_filtrada!![position]  // Sacamos el objeto de la lista

            holder.nombre.text = item_actual.nombre
            holder.precio.text = String.format("%.2f$monedaSimbolo", calcularPrecio(monedaSimbolo, item_actual.precio))
            Glide.with(contexto).load(item_actual.foto).into(holder.imagen)

            holder.comprar.setOnClickListener {
                val intencion = Intent(contexto, EditarProductos::class.java)
                if(!usuario.admin!!) intencion.putExtra("crear", 0)
                else intencion.putExtra("crear", 1)
                intencion.putExtra("producto", item_actual)
                startActivity(contexto, intencion, null)
            }
        }

        override fun getItemCount(): Int = lista_filtrada!!.size

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val busqueda = constraint.toString().lowercase()
                    if (busqueda.length < 3) {
                        lista_filtrada = lista_productos
                    } else {
                        lista_filtrada = (lista_productos.filter {
                            // Se añaden los it (objetos candidatos) si cumplen
                            // una de las condiciones de abajo
                            it.nombre.lowercase().contains(busqueda)
                        }) as MutableList<Producto>
                    }
                    val filterResults = FilterResults()
                    filterResults.values = lista_filtrada
                    return filterResults
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) = notifyDataSetChanged()

            }
        }
}