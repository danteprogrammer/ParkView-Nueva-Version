package com.example.parkview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class dashboard : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var welcomeTextView: TextView
    private lateinit var lastLocationTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onStart() {
        super.onStart()
        loadUserName()
        loadLastLocation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        welcomeTextView = view.findViewById(R.id.welcome_text)
        lastLocationTextView = view.findViewById(R.id.last_location_text)

        val btnSaveLocation = view.findViewById<AppCompatButton>(R.id.btn_save_location)
        val btnSeeCar = view.findViewById<AppCompatButton>(R.id.btn_see_car)
        val btnSeeCameras = view.findViewById<AppCompatButton>(R.id.btn_see_cameras)
        val settingsIcon = view.findViewById<ImageView>(R.id.settings_icon)

        val buttons = listOf(btnSaveLocation, btnSeeCar, btnSeeCameras)
        btnSaveLocation.isSelected = true

        buttons.forEach { button ->
            button.setOnClickListener {
                buttons.forEach { it.isSelected = false }
                it.isSelected = true

                when (it.id) {
                    R.id.btn_save_location -> {
                        findNavController().navigate(R.id.action_dashboard_to_save_location)
                    }
                    R.id.btn_see_car -> {
                        findNavController().navigate(R.id.action_dashboard_to_see_car)
                    }
                    R.id.btn_see_cameras -> {
                        findNavController().navigate(R.id.action_dashboard_to_camaras)
                    }
                }
            }
        }

        settingsIcon.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_settings)
        }
    }

    // --- CÓDIGO ACTUALIZADO Y CORREGIDO ---
    private fun loadLastLocation() {
        val userId = auth.currentUser?.uid ?: return
        val dbRef = database.getReference("locations").child(userId).child("last_location")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Ahora buscamos los nuevos campos: description y timestamp
                    val description = snapshot.child("description").getValue(String::class.java)
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java)

                    if (description != null && timestamp != null) {
                        val tiempoTranscurrido = System.currentTimeMillis() - timestamp
                        val horas = TimeUnit.MILLISECONDS.toHours(tiempoTranscurrido)
                        val minutos = TimeUnit.MILLISECONDS.toMinutes(tiempoTranscurrido) % 60

                        val tiempoStr = when {
                            horas > 0 -> "hace ${horas}h"
                            minutos > 0 -> "hace ${minutos}m"
                            else -> "ahora mismo"
                        }

                        // Mostramos el texto descriptivo y el tiempo
                        lastLocationTextView.text = "Última ubicación: $description $tiempoStr"
                    } else {
                        lastLocationTextView.text = "Datos de ubicación incompletos"
                    }
                } else {
                    lastLocationTextView.text = "No hay ubicación guardada"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                lastLocationTextView.text = "Error al cargar ubicación"
            }
        })
    }
    // --- FIN DE LA ACTUALIZACIÓN ---

    private fun loadUserName() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("users").child(userId).child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val name = snapshot.getValue(String::class.java)
                        welcomeTextView.text = if (name != null) "Bienvenido\n$name" else "Bienvenido\nUsuario"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        welcomeTextView.text = "Bienvenido\nUsuario"
                    }
                })
        } else {
            if (isAdded) {
                findNavController().navigate(R.id.bienvenida)
            }
        }
    }
}