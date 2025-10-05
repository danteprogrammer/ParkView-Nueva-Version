package com.example.parkview

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class save_location : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var selectedX: Float? = null
    private var selectedY: Float? = null
    private var markerView: ImageView? = null
    private lateinit var mapContainer: FrameLayout
    private lateinit var tvEspacioSeleccionado: TextView
    private lateinit var mapaImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_save_location, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        mapContainer = view.findViewById(R.id.map_container)
        tvEspacioSeleccionado = view.findViewById(R.id.tv_espacio_seleccionado)
        mapaImage = view.findViewById(R.id.mapa_image)

        mapaImage.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                selectedX = event.x
                selectedY = event.y
                updateMarkerPosition(selectedX!!, selectedY!!)
                // Actualizamos el texto para mostrar las coordenadas de forma amigable
                tvEspacioSeleccionado.text = String.format(Locale.US, "Espacio en (%.2f, %.2f)", selectedX, selectedY)
            }
            true
        }

        val settingsIcon = view.findViewById<ImageView>(R.id.settings_icon)
        settingsIcon.setOnClickListener {
            findNavController().navigate(R.id.action_save_location_to_settings)
        }

        val btnCancel = view.findViewById<AppCompatButton>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        val btnConfirm = view.findViewById<AppCompatButton>(R.id.btn_confirm_save)
        btnConfirm.isSelected = true
        btnConfirm.setOnClickListener {
            saveLocationToFirebase()
        }
    }

    private fun updateMarkerPosition(x: Float, y: Float) {
        if (markerView == null) {
            markerView = ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.marker_circle))
                mapContainer.addView(this)
            }
        }
        markerView?.let {
            // Centramos el marcador visualmente en el punto exacto del toque
            it.x = x - (it.drawable.intrinsicWidth / 2)
            it.y = y - (it.drawable.intrinsicHeight / 2)
        }
    }

    private fun saveLocationToFirebase() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedX == null || selectedY == null) {
            Toast.makeText(context, "Por favor, selecciona un lugar en el mapa.", Toast.LENGTH_SHORT).show()
            return
        }

        // Normalizamos las coordenadas para que sean independientes del tamaño de la pantalla
        val normalizedX = selectedX!! / mapaImage.width
        val normalizedY = selectedY!! / mapaImage.height
        val timestamp = System.currentTimeMillis()

        // --- CORRECCIÓN CLAVE ---
        // Creamos un texto descriptivo que el dashboard pueda mostrar directamente.
        val description = String.format(Locale.US, "Plano (%.2f, %.2f)", normalizedX, normalizedY)

        val locationData = hashMapOf(
            "description" to description, // <-- AÑADIMOS EL TEXTO
            "normalizedX" to normalizedX,
            "normalizedY" to normalizedY,
            "timestamp" to timestamp
        )

        val dbRef = database.getReference("locations").child(userId).child("last_location")

        dbRef.setValue(locationData)
            .addOnSuccessListener {
                Toast.makeText(context, "Ubicación guardada.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al guardar: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}