package com.example.motorc.productos

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motorc.R
import com.example.motorc.adaptadores.AdaptadorListaProductos
import com.example.motorc.lib.Android.Companion.cargarUsuario
import com.example.motorc.lib.Android.Companion.guardarMoneda
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Producto
import com.example.motorc.modelos.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListaProductos : AppCompatActivity() {

    private lateinit var iv_crear: ImageView
    private lateinit var rb_grupo: RadioGroup
    private lateinit var cb_vehiculos: CheckBox
    private lateinit var cb_repuestos: CheckBox
    private lateinit var cb_mundo: CheckBox
    private lateinit var cb_otro: CheckBox


    private lateinit var recycler: RecyclerView
    private lateinit var adaptador: AdaptadorListaProductos
    private lateinit var usuario: Usuario

    private val ref_bd = FirebaseDatabase.getInstance().getReference()
    private var lista_productos: MutableList<Producto> = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_productos)

        cb_vehiculos = findViewById(R.id.cb_lpr_vehiculos)
        cb_repuestos = findViewById(R.id.cb_lpr_repuestos)
        cb_otro = findViewById(R.id.cb_lpr_otro)

        usuario = cargarUsuario(this)!!

        adaptador = AdaptadorListaProductos(this, applicationContext, lista_productos, usuario)

        recycler = findViewById(R.id.rcy_productos)
        recycler.adapter = adaptador
        iv_crear = findViewById(R.id.iv_lpr_crear)

        if(usuario.admin!!){
            iv_crear.setOnClickListener {
                val intencion = Intent(applicationContext, EditarProductos::class.java)
                intencion.putExtra("crear", 2)
                startActivity(intencion)
            }
        }else iv_crear.visibility = View.GONE

        buscarProds()

        cb_vehiculos.setOnClickListener {
            buscarProds()
        }

        cb_repuestos.setOnClickListener {
            buscarProds()
        }

        cb_otro.setOnClickListener {
            buscarProds()
        }
    }

    fun buscarProds(){
        log("Buscando productos...")

        var lista = mutableListOf(0,1,2)

        if(!cb_vehiculos.isChecked &&
            !cb_repuestos.isChecked &&
            !cb_otro.isChecked){

            ref_bd.child("productos")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        lista_productos.clear()
                        snapshot.children.forEach{ hijo: DataSnapshot?->
                            val pojo_prod=hijo?.getValue(Producto::class.java)!!
                            if((pojo_prod.disponible || usuario.admin!!) && pojo_prod.categoria in lista) lista_productos.add(pojo_prod)
                        }
                        //Notificamos al adaptador del recycler de la vista que hay nuevos cambios
                        recycler.adapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) = println(error.message)
                })
        }else{
            if(!cb_vehiculos.isChecked) lista.remove(0)
            if(!cb_repuestos.isChecked) lista.remove(1)
            if(!cb_otro.isChecked) lista.remove(2)
            ref_bd.child("productos")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        lista_productos.clear()
                        snapshot.children.forEach{ hijo: DataSnapshot?->
                            val pojo_prod=hijo?.getValue(Producto::class.java)!!
                            if((pojo_prod.disponible || usuario.admin!!) && pojo_prod.categoria in lista) lista_productos.add(pojo_prod)

                        }

                        // Notificamos al adaptador del recycler de la vista que hay nuevos cambios
                        recycler.adapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) = println(error.message)
                })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_busqueda,menu)
        val item = menu?.findItem(R.id.action_search)

        val searchView = item?.actionView as androidx.appcompat.widget.SearchView

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                adaptador.filter.filter(newText)
                return true
            }
        })

        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener{

            override fun onMenuItemActionExpand(p0: MenuItem): Boolean = true

            override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                adaptador.filter.filter("")
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

//    override fun onBackPressed() {
//        val intencion = Intent(applicationContext, InicioSuper::class.java)
//        // EXTRA ELIMINADO
//        startActivity(intencion)
//    }
}