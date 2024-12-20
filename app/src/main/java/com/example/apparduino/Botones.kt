package com.example.apparduino

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject

class Botones : Fragment(R.layout.fragment_botones) {

    private val THINGSPEAK_API_KEY = "RUH3TULP6HTN37F9"
    private val THINGSPEAK_WRITE_URL =
        "https://api.thingspeak.com/update"
    private val client = AsyncHttpClient()
    private var isAutomatic = true

    private val handler = Handler()
    private val updateInterval: Long = 1000

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnArriba: Button = view.findViewById(R.id.dejarArriba)
        val btnAbajo: Button = view.findViewById(R.id.dejarAbajo)
        val btnModo: Button = view.findViewById(R.id.btnModo)

        readModeFromThingSpeak()

        btnModo.setOnClickListener {
            isAutomatic = !isAutomatic
            val mode = if (isAutomatic) 0 else 1

            sendToThingSpeak(0, 0, mode)

            Toast.makeText(
                requireContext(),
                "Modo cambiado a: ${if (isAutomatic) "Automático" else "Manual"}",
                Toast.LENGTH_SHORT
            ).show()

            btnArriba.isEnabled = !isAutomatic
            btnAbajo.isEnabled = !isAutomatic
        }

        btnArriba.setOnClickListener {
            sendToThingSpeak(0, 0, 0, 180)  // Enviar 180° al field3
            Toast.makeText(requireContext(), "Servo en posición: Arriba (180°)", Toast.LENGTH_SHORT)
                .show()
        }

        btnAbajo.setOnClickListener {
            sendToThingSpeak(0, 0, 0, 0)  // Enviar 0° al field3
            Toast.makeText(requireContext(), "Servo en posición: Abajo (0°)", Toast.LENGTH_SHORT)
                .show()
        }

        startPeriodicUpdate()
    }
    private var lastUpdateTime: Long = 0
    private fun sendToThingSpeak(distance: Int, servoAngle: Int, mode: Int, appControl: Int = -1) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < 15000) {
            Toast.makeText(requireContext(), "Espere 15 segundos antes de enviar nuevamente.", Toast.LENGTH_SHORT).show()
            return
        }

        lastUpdateTime = currentTime

        val params = RequestParams().apply {
            put("api_key", THINGSPEAK_API_KEY)
            put("field1", distance)
            put("field2", servoAngle)
            if (appControl != -1) {
                put("field3", appControl)
            }
        }

        client.get(THINGSPEAK_WRITE_URL, params, object : TextHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<Header>?, responseBody: String?) {
                println("Respuesta de ThingSpeak: $responseBody")
                Toast.makeText(requireContext(), "Dato enviado correctamente", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseBody: String?, throwable: Throwable?) {
                println("Error al enviar a ThingSpeak: ${throwable?.message}")
                Toast.makeText(requireContext(), "Error: ${throwable?.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun readModeFromThingSpeak() {
        val params = RequestParams().apply {
            put("api_key", THINGSPEAK_API_KEY)
        }

        client.get(
            "https://api.thingspeak.com/channels/2784818/feeds.json",
            params,
            object : TextHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<Header>?,
                    responseBody: String?
                ) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val feeds = jsonResponse.getJSONArray("feeds")

                        if (feeds.length() > 0) {
                            val lastFeed = feeds.getJSONObject(0)
                            val field1 = lastFeed.optString("field1")

                            if (field1.isNotEmpty()) {
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "No hay feeds disponibles en ThingSpeak.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Error al procesar los datos: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header>?,
                    responseBody: String?,
                    throwable: Throwable?
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Error al leer datos: ${throwable?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun startPeriodicUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                readModeFromThingSpeak()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}
