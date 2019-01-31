package com.plumbaria.e_7_2_localizacion

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.TextView

class MainActivity : AppCompatActivity(), LocationListener {
    private companion object {
        // PERMISOS DE UBICACIÓN
        val PERMISOS_LOCALIZACION: Array<String> =
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        // UBICACIÓN
        val TIEMPO_MIN: Long = 10 * 1000 // 10 segundos
        val DISTANCIA_MIN = 4f // 5 metros
        val A:Array<String> = arrayOf("n/d", "preciso", "impreciso")
        val P:Array<String> = arrayOf("n/d", "bajo", "medio", "alto")
        val E:Array<String> = arrayOf("fuera de servicio", "temporalmente no disponible", "disponible")
    }
    // PERMISOS
    val PETICION_LOCALIZACION = 123;
    val PERMISO_LOCALIZACION = Manifest.permission.ACCESS_FINE_LOCATION

    private var manejador : LocationManager? = null
    private var proveedor : String? = null
    private var salida : TextView? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        salida = findViewById(R.id.salida)
        manejador = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!hayPermiso(PERMISO_LOCALIZACION)) {
            // no hay permisos
            ActivityCompat.requestPermissions(
                this,
                PERMISOS_LOCALIZACION,
                PETICION_LOCALIZACION
            )// PEDIMOS LOS PERMISOS
        }else {
            log("Proveedores de localizacion: \n")
            muestraProveedores()
            var criterio:Criteria = Criteria()
            criterio.isCostAllowed = false
            criterio.isAltitudeRequired = false
            criterio.accuracy = Criteria.ACCURACY_FINE
            proveedor = manejador?.getBestProvider(criterio, true)
            log("Mejor proveedor:" + proveedor + "\n")
            log("Comenzamos con la última localización conocida:")

            var localizacion: Location? = manejador?.getLastKnownLocation(proveedor)
            muestraLocalizacion(localizacion)
        }

    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (!hayPermiso(PERMISO_LOCALIZACION)) {
            // no hay permisos
            ActivityCompat.requestPermissions(
                this,
                PERMISOS_LOCALIZACION,
                PETICION_LOCALIZACION
            )// PEDIMOS LOS PERMISOS
        }else {
            manejador?.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN, this)
        }
    }

    override fun onPause() {
        super.onPause()
        manejador?.removeUpdates(this)
    }
    /* MÉTODOS PARA GESTIONAR LOS PERMISOS */

    private fun hayPermiso(permiso:String):Boolean {
        return (
                ContextCompat.checkSelfPermission(this, permiso) ==
                        PackageManager.PERMISSION_GRANTED
                )
    }


    /* EVENTOS LOCALIZACIÓN */

    override fun onLocationChanged(location: Location?) {
        log("Nueva localización:")
        if (location != null) {
            muestraLocalizacion(location)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        log("Cambia estado proveedor: " + proveedor + ", estado=" +
                E[Math.max(0, status)] + ", extras=" + extras + "\n"
        )
    }

    override fun onProviderEnabled(provider: String?) {
        log("Proveedor habilitado: " + proveedor + "\n")
    }

    override fun onProviderDisabled(provider: String?) {
        log("Proveedor deshabilitado: " + proveedor + "\n")
    }

    /* MÉTODOS PARA MOSTRAR INFORMACIÓN */
    fun log(cadena:String ){
        salida?.append(cadena + "\n")
    }

    private fun muestraLocalizacion(localizacion: Location?) {
        if (localizacion == null) {
            log("Localización desconocida \n")
        } else {
            log(localizacion.toString() + "\n")
        }
    }

    private fun muestraProveedores() {
        log("Proveedor de localizción: \n")
        var proveedores:List<String> = manejador!!.allProviders
        for(proveedor in proveedores){
            muestraProveedor(proveedor)
        }
    }

    private fun muestraProveedor(proveedor: String) {
        var info : LocationProvider = manejador!!.getProvider(proveedor)
        log("LocationProveider[ getName = " + info.name
                + ", isProvederEnabled="
                + manejador!!.isProviderEnabled(proveedor) + ", getAcuracy="
                + A [Math.max(0, info.accuracy)] + ", getPowerRequirement="
                + P [Math.max(0, info.powerRequirement)]
                + ", hasMonetaryCost=" + info.hasMonetaryCost()
                + ", requiresCell=" + info.requiresCell()
                + ",requiresNetwork=" + info.requiresNetwork()
                + ", supportsAltitude=" + info.supportsAltitude()
                + ", supportsBearing="+ info.supportsBearing()
                + ", supportsSpeed" + info.supportsSpeed() + " ]\n"
        )
    }


}
