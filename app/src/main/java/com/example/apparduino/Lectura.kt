package com.example.apparduino

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject

class Lectura : Fragment() {

    private val THINGSPEAK_API_KEY = "TU0W1R5EK0GQTE3O"
    private val THINGSPEAK_READ_URL = "https://api.thingspeak.com/channels/2784818/feeds.json"
    private val client = AsyncHttpClient()
    private lateinit var tvDistancia: TextView
    private val handler = Handler()
    private val updateInterval: Long = 1000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lectura, container, false)
        tvDistancia = view.findViewById(R.id.tvDistancia)

        startPeriodicUpdate()

        return view
    }

    private fun readDistanceFromThingSpeak() {
        val params = RequestParams()
        params.put("api_key", THINGSPEAK_API_KEY)

        client.get(THINGSPEAK_READ_URL, params, object : TextHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<Header>?, responseBody: String?) {
                if (statusCode == 200) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val feeds = jsonResponse.getJSONArray("feeds")

                        if (feeds.length() > 0) {
                            val lastFeed = feeds.getJSONObject(feeds.length() - 1)
                            val distance = lastFeed.optString("field1") // Leer el valor de field1

                            if (distance.isNotEmpty()) {
                                tvDistancia.text = "Distancia: $distance cm"
                            } else {
                                tvDistancia.text = "No hay datos disponibles"
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error al leer la distancia: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta de la API: $statusCode", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseBody: String?, throwable: Throwable?) {
                Toast.makeText(requireContext(), "Error al leer datos: ${throwable?.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startPeriodicUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                readDistanceFromThingSpeak()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}
