package com.example.motorc.lib

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.toColor
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.motorc.R
import com.example.motorc.modelos.Evento
import com.example.motorc.modelos.Usuario
import com.example.motorc.usuarios.EditarUsuario
import java.lang.Exception

class Herramientas {

    companion object{

        fun adaptarEmail(email: String) : String {
            try{
                val email_formateado= email.split("@")
                return "${email_formateado[0]}@${email_formateado[1].replace(".","_")}"
            }catch (e: Exception){
                return ""
            }
        }

        fun humanizarEmail(email: String): String{
            try{
                val email_formateado= email.split("@")
                return "${email_formateado[0]}@${email_formateado[1].replace("_",".")}"
            }catch (e: Exception){
                return ""
            }
        }

        fun actualizarAppBar(contexto: Context, usuario: Usuario, ab_avatar: ImageView, ab_nombre: TextView, ab_admin: ImageView){

            adaptarAvatar(contexto, usuario.foto_url).into(ab_avatar)


            ab_nombre.text = usuario.nombre

            if(!usuario.admin!!){
                ab_admin.visibility = View.GONE
            }
        }

        fun actualizarAppBar(contexto: Context,cliente: Usuario, usuario: Usuario, ab_avatar: ImageView, ab_nombre: TextView,){

            adaptarAvatar(contexto, usuario.foto_url).into(ab_avatar)
            ab_avatar.setOnClickListener {
                val intencion = Intent(contexto, EditarUsuario::class.java)

                intencion.putExtra("revisar", usuario)
                intencion.putExtra("usuario", cliente)
                intencion.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(contexto, intencion, null)
            }

            ab_nombre.text = usuario.nombre
        }

        fun actualizarAppBar(contexto: Context, usuario: Usuario, evento: Evento, ab_foto: ImageView, ab_titulo: TextView, ab_desc: TextView){
//
//            adaptarAvatar(contexto, evento.img_url).into(ab_foto)
//            ab_foto.setOnClickListener {
//                val intencion = Intent(contexto, EditarTema::class.java)
//
//                // EXTRA ELIMINADO
//                intencion.putExtra("tema", tema)
//                intencion.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(contexto, intencion, null)
//            }
//            ab_titulo.text = tema.titulo
//
//            ab_desc.text = tema.desc
        }

        fun adaptarAvatar(contexto: Context, img_url: String?): RequestBuilder<Drawable> {
//            val opciones = if(img_url != "") RequestOptions.bitmapTransform(CircleCrop()).placeholder(R.drawable.man).circleCrop()
//            return Glide.with(contexto)
//                .load(img_url)
//                .apply(opciones)

            if(img_url != "") return Glide.with(contexto)
                                            .load(img_url)
                                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            else return Glide.with(contexto)
                                .load(R.drawable.man)
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
        }

        fun adaptarAvatar(contexto: Context, img_res: Int?): RequestBuilder<Drawable> {
            val opciones= RequestOptions.bitmapTransform(CircleCrop()).placeholder(R.drawable.man)
            return Glide.with(contexto)
                .load(img_res)
                .apply(opciones)
        }

        /** Imprimir en consola
         *
         * n => normal      (blanco)
         *
         * a => advertencia (amarillo)
         *
         * e => error       (rojo)
         */
        fun log(t: String, msg: String){
            if(t == "n") Log.i("INFO", msg)
            else if(t == "a") Log.println(Log.WARN,"ADVERTENCIA", msg)
            else if(t == "e") Log.e("ERROR", msg)
        }

        fun log(msg: String){
            Log.i("INFO", msg)
        }
    }
}