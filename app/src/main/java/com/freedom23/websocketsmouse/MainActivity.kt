package com.freedom23.websocketsmouse

import android.content.DialogInterface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleRegistry
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import okhttp3.OkHttpClient


class MainActivity : AppCompatActivity() {

    private var wsURL : String? = null
    private var scarlet : Scarlet? = null
    private var httpClient : OkHttpClient? = null
    private var mouseServer : MouseServer? = null
    private lateinit var nsdManager : NsdManager

    private lateinit var btnKreni : Button
    private lateinit var btnStani : Button
    private lateinit var sensorManager : SensorManager
    private lateinit var gyro : Sensor
    private lateinit var btnLevi : Button
    private lateinit var btnDesni : Button
    private lateinit var btnCentriraj : Button

    private fun catchError(t: Throwable) {
        val builder = AlertDialog.Builder(this)
        with (builder) {
            setTitle("Error grdni!")
            setMessage("${t.message}")
            setNeutralButton("Jbg", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    Toast.makeText(applicationContext,
                        "???", Toast.LENGTH_SHORT).show()

                }

            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnKreni = findViewById(R.id.btnKreni)
        btnStani = findViewById(R.id.btnStani)
        btnLevi = findViewById(R.id.btnLevi)
        btnDesni = findViewById(R.id.btnDesni)
        btnCentriraj = findViewById(R.id.btnCenter)

        httpClient = OkHttpClient();
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        nsdManager = getSystemService(NSD_SERVICE) as NsdManager

        val resolveListener : NsdManager.ResolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(p0: NsdServiceInfo?, p1: Int) {
                Log.e("NSD", "resolve failed")
            }

            override fun onServiceResolved(p0: NsdServiceInfo?) {
                Log.e("NSD", "ip ${p0?.host}:${p0?.port}")
                if (p0?.serviceName.toString() == "NintendoWiid") {
                    wsURL = "ws://${p0?.host}:${p0?.port}"
                }
            }

        }


        val discoveryListener : NsdManager.DiscoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
                Log.e("NSD", "start discovery failed, $p0 $p1")
            }

            override fun onStopDiscoveryFailed(p0: String?, p1: Int) {
                Log.e("NSD", "stop discovery failed")
            }

            override fun onDiscoveryStarted(p0: String?) {
                Log.d("NSD", "discovery started")
            }

            override fun onDiscoveryStopped(p0: String?) {
                Log.d("NSD", "discovery stopped")
            }

            override fun onServiceFound(service: NsdServiceInfo?) {
                Log.e("NSD", "${service?.serviceName.toString()} found")
                nsdManager.resolveService(service, resolveListener)

            }

            override fun onServiceLost(p0: NsdServiceInfo?) {
                Log.e("NSD", "${p0?.serviceName.toString()} lost")
            }

        }

        val gyroListener : SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(p0: SensorEvent?) {
                val message = "${p0?.values?.get(0)} ${p0?.values?.get(1)} ${p0?.values?.get(2)}"
                mouseServer?.sendGyro(message)
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                Log.d("GYRO", "Accuracy changed")
            }

        }

        try {
            nsdManager.discoverServices(
                "_http._tcp.",
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener)


            gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        } catch (t: Throwable) {
            catchError(t)
        }


        btnKreni.setOnClickListener {

            try {
                scarlet = Scarlet.Builder()
                    .webSocketFactory(httpClient!!.newWebSocketFactory(wsURL.toString()))
                    .build()
                mouseServer = scarlet?.create<MouseServer>()
                sensorManager.registerListener(gyroListener, gyro, SensorManager.SENSOR_DELAY_GAME)
            } catch (t: Throwable) {
                catchError(t)
            }


        }

        btnLevi.setOnClickListener { mouseServer?.levi() }

        btnDesni.setOnClickListener { mouseServer?.desni() }



        btnCentriraj.setOnClickListener() { mouseServer?.recenter() }

        btnStani.setOnClickListener {
            sensorManager.unregisterListener(gyroListener)
            mouseServer?.close()
            recreate()
        }


    }


}