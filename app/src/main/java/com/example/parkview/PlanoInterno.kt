package com.example.parkview

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
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

class PlanoInterno : Fragment() {

    // 1. Restauramos la conexión a Firebase (necesaria para la función de borrar)
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // 2. Restauramos todas las vistas del XML
    private lateinit var mapContainer: FrameLayout
    private lateinit var mapaImage: ImageView
    private lateinit var locationTextView: TextView
    private var markerView: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plano_interno, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inicializamos todas las vistas que usaremos
        mapContainer = view.findViewById(R.id.map_container_plano)
        mapaImage = view.findViewById(R.id.mapa_image_plano)
        locationTextView = view.findViewById(R.id.location_text)

        // 3. Obtenemos los datos que nos envió 'see_car.kt' desde el Bundle
        val normX = arguments?.getFloat("normalizedX")
        val normY = arguments?.getFloat("normalizedY")

        // 4. Usamos los datos recibidos para mostrar el marcador (ya no cargamos desde Firebase)
        if (normX != null && normY != null) {
            locationTextView.text = "Tu auto está aquí"
            // Usamos 'post' para asegurarnos de que el layout ya tenga dimensiones y no falle
            mapContainer.post {
                showMarkerAtPosition(normX, normY)
            }
        } else {
            // Esto se mostrará si por alguna razón los datos no llegan
            locationTextView.text = "No se pudo recibir la ubicación"
        }

        // --- 5. Restauramos la funcionalidad de TODOS los botones ---
        val btnActualizar = view.findViewById<AppCompatButton>(R.id.btn_actualizar_ubicacion)
        btnActualizar.setOnClickListener {
            findNavController().navigate(R.id.action_planoInterno_to_save_location)
        }

        val btnBorrar = view.findViewById<AppCompatButton>(R.id.btn_borrar_ubicacion_plano)
        btnBorrar.setOnClickListener { showDeleteConfirmationDialog() }

        val btnVerCamaras = view.findViewById<AppCompatButton>(R.id.btn_ver_camaras_plano)
        btnVerCamaras.setOnClickListener {
            findNavController().navigate(R.id.action_planoInterno_to_camaras)
        }

        val btnRegresar = view.findViewById<AppCompatButton>(R.id.btn_regresar_plano)
        btnRegresar.setOnClickListener { findNavController().popBackStack() }

        val settingsIcon = view.findViewById<ImageView>(R.id.settings_icon)
        settingsIcon.setOnClickListener {
            findNavController().navigate(R.id.action_planoInterno_to_settings)
        }
    }

    // 6. Esta función dibuja el marcador en la posición correcta
    private fun showMarkerAtPosition(normalizedX: Float, normalizedY: Float) {
        // Removemos un marcador anterior si existe, para evitar duplicados
        markerView?.let { mapContainer.removeView(it) }

        // Calculamos la posición en píxeles usando el tamaño de la imagen, que es más preciso
        val pixelX = normalizedX * mapaImage.width
        val pixelY = normalizedY * mapaImage.height

        markerView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.marker_circle))
        }
        mapContainer.addView(markerView)

        // Usamos 'post' de nuevo para centrar el marcador correctamente
        markerView?.post {
            markerView?.x = pixelX - (markerView?.width ?: 0) / 2
            markerView?.y = pixelY - (markerView?.height ?: 0) / 2
        }
    }

    // 7. Restauramos las funciones de borrado que dependen de Firebase
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Borrar Ubicación")
            .setMessage("¿Estás seguro de que deseas borrar la ubicación guardada?")
            .setPositiveButton("Sí, borrar") { _, _ -> deleteLocation() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteLocation() {
        val userId = auth.currentUser?.uid ?: return
        val dbRef = database.getReference("locations").child(userId).child("last_location")

        dbRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Ubicación borrada.", Toast.LENGTH_SHORT).show()
                markerView?.let { mapContainer.removeView(it) }
                locationTextView.text = "No hay ubicación guardada"
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al borrar la ubicación.", Toast.LENGTH_SHORT).show()
            }
    }
}