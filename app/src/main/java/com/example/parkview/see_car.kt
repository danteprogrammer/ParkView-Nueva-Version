package com.example.parkview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class see_car : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var tvPiso: TextView
    private lateinit var tvLugar: TextView

    private var loadedNormX: Float? = null
    private var loadedNormY: Float? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_see_car, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        tvPiso = view.findViewById(R.id.tv_piso)
        tvLugar = view.findViewById(R.id.tv_lugar)

        val btnGoogleMaps = view.findViewById<ImageView>(R.id.btn_google_maps)
        btnGoogleMaps.setOnClickListener { openGoogleMaps() }

        val btnPlanoInterno = view.findViewById<TextView>(R.id.btn_ver_plano)
        btnPlanoInterno.setOnClickListener {
            if (loadedNormX != null && loadedNormY != null) {
                // --- CAMBIO CLAVE AQUÍ ---
                // 1. Creamos un Bundle para guardar nuestros datos.
                val bundle = Bundle().apply {
                    putFloat("normalizedX", loadedNormX!!)
                    putFloat("normalizedY", loadedNormY!!)
                }
                // 2. Navegamos a la acción y le pasamos el Bundle.
                findNavController().navigate(R.id.action_see_car_to_planoInterno, bundle)
            } else {
                Toast.makeText(context, "Ubicación no disponible para ver en plano.", Toast.LENGTH_SHORT).show()
            }
        }

        val settingsIcon = view.findViewById<ImageView>(R.id.settings_icon)
        settingsIcon.setOnClickListener {
            findNavController().navigate(R.id.action_see_car_to_settings)
        }

        val btnRegresar = view.findViewById<AppCompatButton>(R.id.btn_regresar_see_car)
        btnRegresar.setOnClickListener {
            findNavController().popBackStack()
        }

        loadCarLocation()
    }

    private fun loadCarLocation() {
        val userId = auth.currentUser?.uid ?: return
        val dbRef = database.getReference("locations").child(userId).child("last_location")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val normX = snapshot.child("normalizedX").getValue(Double::class.java)
                    val normY = snapshot.child("normalizedY").getValue(Double::class.java)

                    if (normX != null && normY != null) {
                        loadedNormX = normX.toFloat()
                        loadedNormY = normY.toFloat()
                        tvPiso.text = "Ubicación Guardada"
                        tvLugar.text = "Ver en el plano para detalles"
                    } else {
                        tvPiso.text = "Error al leer"
                        tvLugar.text = "las coordenadas"
                    }
                } else {
                    tvPiso.text = "No hay ubicación"
                    tvLugar.text = "guardada"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar la ubicación.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openGoogleMaps() {
        val latitud = "-12.0853"
        val longitud = "-77.0504"
        val label = "Estacionamiento ParkView"

        val gmmIntentUri = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud($label)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        val webUri = Uri.parse("https://maps.google.com/maps?q=loc:$latitud,$longitud($label)")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)

        try {
            startActivity(mapIntent)
        } catch (e: Exception) {
            startActivity(webIntent)
        }
    }
}