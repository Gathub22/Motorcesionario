package com.example.motorc.productos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.motorc.R
import com.example.motorc.lib.Android
import com.example.motorc.lib.Android.Companion.calcularPrecio
import com.example.motorc.lib.Android.Companion.cargarUsuario
import com.example.motorc.lib.Android.Companion.mostrarAviso
import com.example.motorc.lib.Android.Companion.verMoneda
import com.example.motorc.lib.Firebase.Companion.enviarProducto
import com.example.motorc.lib.Firebase.Companion.guardarFoto
import com.example.motorc.lib.Firebase.Companion.verProducto
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Producto
import com.example.motorc.modelos.Usuario
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch

class EditarProductos: AppCompatActivity() {

    lateinit var iv_foto: ImageView
    lateinit var et_nombre: EditText
    lateinit var et_precio: EditText
    lateinit var rg_tipos: RadioGroup
    lateinit var rb_veh: RadioButton
    lateinit var rb_rep: RadioButton
    lateinit var rb_otr: RadioButton
    lateinit var et_desc: EditText
    lateinit var bt_accion: Button
    lateinit var sw_disponible: Switch

    lateinit var usuario: Usuario
    lateinit var producto: Producto

    var foto_uri: Uri? = null
    var prod_id: String? = null
    var crear: Int = 0

    val ref_bd = FirebaseDatabase.getInstance().getReference()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_producto)

        usuario = cargarUsuario(this)!!

        try {
            producto = intent.getParcelableExtra("producto")!!
        }catch (e: Exception){
            log("e", "No hay producto para mostrar")
        }

        crear = intent.getIntExtra("crear", 0)

        iv_foto = findViewById(R.id.iv_pro_foto)
        et_nombre = findViewById(R.id.et_pro_nombre)
        et_precio = findViewById(R.id.et_pro_precio)
        rg_tipos = findViewById(R.id.rg_por_tipos)
        rb_veh = findViewById(R.id.rb_por_vehiculo)
        rb_rep = findViewById(R.id.rb_por_repuesto)
        rb_otr = findViewById(R.id.rb_por_otro)
        et_desc = findViewById(R.id.et_por_desc)
        bt_accion = findViewById(R.id.bt_pro_comprar)
        sw_disponible = findViewById(R.id.sw_pro_disponible)

        if(crear == 0){
            log("Iniciando modo compra...")

            rg_tipos.visibility = View.GONE
            sw_disponible.visibility = View.GONE
            et_nombre.focusable = View.NOT_FOCUSABLE
            et_desc.focusable = View.NOT_FOCUSABLE
            et_precio.focusable = View.NOT_FOCUSABLE

            Glide.with(this).load(producto.foto).into(iv_foto)
            et_nombre.setText(producto.nombre)
            et_desc.setText(producto.desc)

            val m = verMoneda(this)
            val precio = String.format("%.2f$m", calcularPrecio(m, producto.precio))

            et_precio.setText(precio)
            bt_accion.setText("COMPRAR $precio")
//            bt_accion.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icono_catalogo,0,0,0)
            bt_accion.setOnClickListener {
                if(usuario.direccion != ""){
                    val intencion = Intent(applicationContext, FormPedido::class.java)
                    intencion.putExtra("producto", producto)
                    startActivity(intencion)
                    finish()
                }else mostrarAviso(bt_accion, "No puedes comprar sin una dirección asignada")
            }

        }else{
            log("Iniciando modo edicion...")

            iv_foto.setOnClickListener {
                val ficheroTemp = crearFicheroImagen()
                foto_uri = FileProvider.getUriForFile(
                    applicationContext,
                    "com.example.motorc.fileprovider",
                    ficheroTemp
                )
                getCamara.launch(foto_uri)
            }
            iv_foto.setOnLongClickListener {
                getGaleria.launch("image/*")
                true
            }

            if(crear == 1){
                log("Mostrando datos del producto...")

                Glide.with(applicationContext).load(producto.foto).into(iv_foto)
                et_nombre.setText(producto.nombre)
                et_desc.setText(producto.desc)
                et_precio.setText(String.format("%.2f", producto.precio))
                sw_disponible.isChecked = producto.disponible

                if      (producto.categoria == 0)   rb_veh.isChecked = true
                else if (producto.categoria == 1)   rb_rep.isChecked = true
                else                                rb_otr.isChecked = true
                prod_id = producto.id
            }else{
                sw_disponible.isChecked = true
            }

            bt_accion.setText("GUARDAR")
//            bt_accion.setCompoundDrawablesWithIntrinsicBounds(R.drawable.capa_icono_guardar,0,0,0)
            bt_accion.setOnClickListener {
                GlobalScope.launch (Dispatchers.IO){

                    log("Comprobando producto...")
                    var valido = true

                    val nombre = et_nombre.text.toString().trim()
                    if(nombre.length < 4){
                        valido = false
                        et_nombre.error = "Nombre demasiado corto"
                    }else if(nombre.length > 30){
                        valido = false
                        et_nombre.error = "Nombre demasiado largo"
                    }else{
                        val s= CountDownLatch(1)
                        verProducto("nombre", nombre){
                            if(it != null){
                                if(it.id != producto.id){
                                    valido = false
                                    et_nombre.error = "El producto ya existe"
                                }
                            }
                            s.countDown()
                        }
                        s.await()
                    }

                    val desc = et_desc.text.toString().trim()
                    if(desc.length < 10){
                        valido = false
                        et_desc.error = "Descripción demasiado corta"
                    }else if(desc.length > 500){
                        valido = false
                        et_desc.error = "Descripción demasiado larga"
                    }

                    var precio : Double? = null
                    try{
                        precio = et_precio.text.toString().toDouble()

                        if(precio < 0){ valido = false }
                    }catch (e: Exception){
                        valido = false
                        log("a", "Conversión del precio fallida")
                    }

                    var tipo: Int? = null
                    if      (rb_veh.isChecked)  tipo = 0
                    else if (rb_rep.isChecked)  tipo = 1
                    else                        tipo = 2

                    if(valido){


                            val disp = sw_disponible.isChecked
                            val prod = Producto("", disp, nombre, desc, precio!!, tipo, "")
                            if(prod_id != null){
                                prod.id = prod_id as String
                            }else{
                                log("Creando nuevo id para el producto...")
                                prod.id = ref_bd.child("productos").push().key.toString()
                            }

                            if(foto_uri != null){
                                val foto_url = guardarFoto("productos", foto_uri)
                                prod.foto = foto_url
                            }else{
                                prod.foto = producto.foto
                            }

                            val semaforo = CountDownLatch(1)
                            enviarProducto(prod)

                            foto_uri = null
                            prod_id = prod.id

                            producto = prod
                            mostrarAviso(rb_veh, "Producto guardado correctamente")

                    }else{
                        log("n", "Producto inválido. Guardado cancelado")
                    }
                }
            }
        }
    }

    val getCamara=registerForActivityResult(ActivityResultContracts.TakePicture()){

        if(it){
            iv_foto.setImageURI(foto_uri as Uri?)
        }else{
            Snackbar.make(iv_foto,
                R.string.sinfoto,
                Snackbar.LENGTH_LONG).show()
        }
    }

    val getGaleria=registerForActivityResult(ActivityResultContracts.GetContent()){

        // it es un valor que acompaña a la función. Si esa función no guarda ningún valor, que avise
        if(it==null){
            Android.mostrarAviso(iv_foto, resources.getString(R.string.sinfoto))
        }else{
            // Si el it tiene una ruta de un archivo (el URI), que lo cambie en el ImageView
            foto_uri = it
            iv_foto.setImageURI(it)
        }
    }

    private fun crearFicheroImagen(): File {
        val cal: Calendar?= Calendar.getInstance()

        val timeStamp:String?= SimpleDateFormat("yyyyMMdd_HHmmss").format(cal!!.time)
        val nombreFichero:String?="JPEG_"+timeStamp+"_"
        val carpetaDir: File?=applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val ficheroImagen: File?= File.createTempFile(nombreFichero!!,".jpg",carpetaDir)

        return ficheroImagen!!
    }
}