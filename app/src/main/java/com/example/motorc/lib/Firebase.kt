package com.example.motorc.lib

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import com.example.motorc.Inicio
import com.example.motorc.lib.Herramientas.Companion.adaptarEmail
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Pedido
import com.example.motorc.modelos.Producto
import com.example.motorc.modelos.Usuario
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class Firebase {

    companion object{
        val ref_bd = FirebaseDatabase.getInstance().getReference()
        private val ref_sto = FirebaseStorage.getInstance().getReference()

        fun enviarUsuario(id: String, dir_email: String, nombre: String, dir_img_fb: String, direccion: String, admin: Boolean, fecha_creacion: String, contrasena: String, disponible: Boolean){
            val email_formateado = adaptarEmail(dir_email)
            ref_bd.child("usuarios")
                .child(email_formateado)
                .setValue(Usuario(id, email_formateado, nombre,dir_img_fb, direccion,  admin, fecha_creacion, contrasena, disponible))
            log("n","Usuario enviado a Firebase")
        }

        fun enviarUsuario(usuario: Usuario){
            usuario.email = adaptarEmail(usuario.email!!)
            ref_bd.child("usuarios")
                .child(usuario.id!!)
                .setValue(usuario)
            log("n","Usuario enviado a Firebase")
        }

        fun enviarProducto(producto: Producto){
            ref_bd.child("productos")
                .child(producto.id)
                .setValue(producto)
            log("n","Producto enviado a Firebase")
        }

        fun enviarPedido(pedido: Pedido){
            ref_bd.child("pedidos")
                .child(pedido.id)
                .setValue(pedido)
            log("n","Pedido enviado a Firebase")
        }


        fun verUsuario(tipo: String,id: String, salida: (Usuario?) -> Unit){
            var usuario: Usuario? = null

            if(tipo == "id"){
                log("Buscando usuario (id:$id)...")
                val usuarioRef = ref_bd.child("usuarios").child(id)
                usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        usuario = dataSnapshot.getValue(Usuario::class.java)
                        salida(usuario)
                    }

                    override fun onCancelled(databaseError: DatabaseError){
                        log("e",databaseError.message)
                        salida(null)
                    }
                })
            }else if(tipo == "email"){
                log("Buscando usuario (email:$id)...")

                ref_bd.child("usuarios")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.children.forEach { hijo: DataSnapshot? ->
                                val tempUsr = hijo?.getValue(Usuario::class.java)
                                if(id == tempUsr!!.email)
                                    usuario = tempUsr
                            }
                            salida(usuario)
                        }

                        override fun onCancelled(error: DatabaseError){
                            log("e", error.toString())
                            salida(null)
                        }
                    })
            }else if(id=="nombre"){
                log("Buscando usuario (nombre:$id)...")

                ref_bd.child("usuarios")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.children.forEach { hijo: DataSnapshot? ->
                                val tempUsr = hijo?.getValue(Usuario::class.java)
                                if(id == tempUsr!!.nombre)
                                    usuario = tempUsr
                            }
                            salida(usuario)
                        }

                        override fun onCancelled(error: DatabaseError){
                            log("e", error.toString())
                            salida(null)
                        }
                    })
            }else salida(null)
        }

        fun verProducto(tipo: String, id: String, salida: (Producto?) -> Unit){
            if(tipo == "id"){
                log("Buscando producto (id:$id)...")
                var producto: Producto?

                val productoRef = ref_bd.child("productos").child(id)
                productoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        producto = dataSnapshot.getValue(Producto::class.java)
                        salida(producto)
                    }

                    override fun onCancelled(databaseError: DatabaseError){
                        log("e",databaseError.message)
                        salida(null)
                    }
                })
            }else if(tipo =="nombre"){
                log("Buscando producto (nombre:$id)...")

                var producto: Producto? = null
                ref_bd.child("productos")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.children.forEach { hijo: DataSnapshot? ->
                                val tempPro = hijo?.getValue(Producto::class.java)
                                if(id == tempPro!!.nombre)
                                    producto = tempPro
                            }
                            salida(producto)
                        }

                        override fun onCancelled(error: DatabaseError){
                            log("e", error.toString())
                            salida(null)
                        }
                    })
            }else salida(null)
        }

        suspend fun guardarFoto(direccion: String, id:String?, dir_foto: Uri?):String{
            lateinit var url_foto_firebase: Uri

            url_foto_firebase = ref_sto.child("fotos")
                .child(direccion)
                .child(id!!)
                .putFile(dir_foto!!)
                .await()
                .storage
                .downloadUrl
                .await()

            log("n","Foto guardada ($url_foto_firebase)")
            return url_foto_firebase.toString()
        }

        suspend fun guardarFoto(direccion: String, dir_foto: Uri?):String{
            log("Guardando foto en $direccion...")

            lateinit var url_foto_firebase: Uri

            val id = ref_bd.child(direccion).push().key

            url_foto_firebase = ref_sto.child("fotos")
                .child(direccion)
                .child(id!!)
                .putFile(dir_foto!!)
                .await()
                .storage
                .downloadUrl
                .await()
            log("n","Foto guardada ($url_foto_firebase)")
            return url_foto_firebase.toString()
        }
    }
}