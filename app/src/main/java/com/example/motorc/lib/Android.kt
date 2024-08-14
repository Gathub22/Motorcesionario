package com.example.motorc.lib

import android.content.SharedPreferences
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.motorc.R
import com.example.motorc.lib.Herramientas.Companion.log
import com.example.motorc.modelos.Usuario
import com.google.android.material.snackbar.Snackbar

class Android {
    companion object{

        val monedas = hashMapOf<String, Double>(
            "€" to 1.0,
            "$" to 1.075,
            "¥" to 149.822,
            "₽" to 88.913,
            "£" to 0.854
        )

        fun accederDatos(actividad : AppCompatActivity): SharedPreferences {
            log("Accediendo datos de la memoria...")
            return actividad.getSharedPreferences("${actividad.getString(R.string.app_id)}_DATOS", 0)
        }

        fun cargarUsuario(actividad : AppCompatActivity): Usuario? {
            val datos = accederDatos(actividad)

            val id = datos.getString("id", null)
            val email = datos.getString("email", null)
            val nom = datos.getString("nombre", null)
            val foto_url = datos.getString("foto_url", null)
            val direccion = datos.getString("direccion", null)
            val admin = datos.getBoolean("admin", false)
            val fecha_creacion = datos.getString("fecha", null)
            val contrasena = datos.getString("contrasena", null)
            val disponible = datos.getBoolean("disponible", false)

            if (id == null){
                log("a", "¡Usuario no encontrado! Devolviendo null...")
                return null
            }

            log("Usuario encontrado")
            return Usuario(id, email, nom, foto_url, direccion, admin, fecha_creacion, contrasena, disponible)
        }

        fun guardarUsuario(actividad: AppCompatActivity, usuario: Usuario){
            val datos = accederDatos(actividad)

            with(datos.edit()){
                putString("id", usuario.id)
                putString("email", usuario.email)
                putString("nombre", usuario.nombre)
                putString("foto_url", usuario.foto_url)
                putString("direccion", usuario.direccion)
                putBoolean("admin", usuario.admin!!)
                putString("fecha", usuario.fecha_creacion)
                putString("contrasena", usuario.contrasena)
                putBoolean("disponible", usuario.disponible)
                commit()
            }

            log("n","Usuario guardado en memoria")
        }


        fun mostrarAviso(vista: View, mensaje: String) = Snackbar.make(vista, mensaje, Snackbar.LENGTH_LONG).show()

        fun guardarMoneda(actividad: AppCompatActivity, m: String){
            log("Guardando moneda: $m")

            accederDatos(actividad).edit().putString("moneda", m).commit()

            log("Moneda guardada con éxito")
        }

        fun verMoneda(actividad: AppCompatActivity): String {
            return accederDatos(actividad).getString("moneda", "€")!!
        }

        fun calcularPrecio(actividad: AppCompatActivity, precio: Double): Double{
            log("Calculando precio ($precio €)...")

            return precio * monedas[verMoneda(actividad)]!!
        }
        fun calcularPrecio(moneda: String, precio: Double): Double{
            log("Calculando precio ($precio € a $moneda)...")

            return precio * monedas[moneda]!!
        }
    }

}