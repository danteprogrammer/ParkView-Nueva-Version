package com.example.parkview

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

class ReproductorCamara : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reproductor_camara, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recibimos los datos de la cámara seleccionada
        val cameraName = arguments?.getString("cameraName")
        val streamUrl = arguments?.getString("streamUrl")

        val tvCameraName = view.findViewById<TextView>(R.id.tv_reproductor_title)
        tvCameraName.text = cameraName ?: "Cámara no encontrada"

        // --- Listeners de los botones ---
        val btnPlay = view.findViewById<AppCompatButton>(R.id.btn_play)
        btnPlay.setOnClickListener {
            Toast.makeText(context, "Iniciando reproducción...", Toast.LENGTH_SHORT).show()
            // Aquí iría la lógica para iniciar el video con el 'streamUrl'
        }

        val btnPause = view.findViewById<AppCompatButton>(R.id.btn_pause)
        btnPause.setOnClickListener {
            Toast.makeText(context, "Video pausado", Toast.LENGTH_SHORT).show()
        }

        val btnMute = view.findViewById<AppCompatButton>(R.id.btn_mute)
        btnMute.setOnClickListener {
            Toast.makeText(context, "Silencio activado/desactivado", Toast.LENGTH_SHORT).show()
        }

        val btnFullscreen = view.findViewById<AppCompatButton>(R.id.btn_fullscreen)
        btnFullscreen.setOnClickListener {
            Toast.makeText(context, "Entrando/saliendo de pantalla completa", Toast.LENGTH_SHORT).show()
        }

        val btnCambiarCamara = view.findViewById<AppCompatButton>(R.id.btn_cambiar_camara)
        btnCambiarCamara.setOnClickListener {
            // Esta acción nos lleva de vuelta a la lista de cámaras
            findNavController().popBackStack()
        }

        val settingsIcon = view.findViewById<ImageView>(R.id.settings_icon)
        settingsIcon.setOnClickListener {
            findNavController().navigate(R.id.action_reproductorCamara_to_settings)
        }
    }
}