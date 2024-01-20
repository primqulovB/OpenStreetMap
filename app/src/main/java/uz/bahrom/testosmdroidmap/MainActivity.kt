package uz.bahrom.testosmdroidmap

import android.content.pm.PackageManager
import android.graphics.Rect
import android.location.GpsStatus
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import uz.bahrom.testosmdroidmap.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), MapListener, GpsStatus.Listener {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var map: MapView
    private lateinit var controller: IMapController
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var binding: ActivityMainBinding
    private lateinit var mapPoint: GeoPoint
    private lateinit var marker: Marker
    private val latitude = 38.84505571861153
    private val longitude = 65.79231262207031

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        )
        map = binding.map
        marker = Marker(map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.mapCenter
        map.setMultiTouchControls(true)
        map.getLocalVisibleRect(Rect())


        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        controller = map.controller

        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true
        myLocationOverlay.runOnFirstFix {
            runOnUiThread {
                controller.setCenter(myLocationOverlay.myLocation);
                controller.animateTo(myLocationOverlay.myLocation)
            }
        }
        mapPoint = GeoPoint(latitude, longitude)

        controller.setZoom(12.0)

        /*Log.e("TAG", "onCreate:in ${controller.zoomIn()}")
        Log.e("TAG", "onCreate: out  ${controller.zoomOut()}")*/

        controller.animateTo(mapPoint)
        map.overlays.add(myLocationOverlay)

        map.addMapListener(this)


        this.myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        this.myLocationOverlay.enableMyLocation()
        map.overlays.add(this.myLocationOverlay)

    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        event?.source?.mapCenter

        val latitude = event?.source?.mapCenter?.latitude as Double
        val longitude = event.source?.mapCenter?.longitude as Double
       /* Log.e("TAG", "onCreate:la ${event?.source?.getMapCenter()?.latitude}")
        Log.e("TAG", "onCreate:lo ${event?.source?.getMapCenter()?.longitude}")
        Log.e("TAG", "onScroll   x: ${event?.x}  y: ${event?.y}")*/
        val geoPoint = GeoPoint(latitude, longitude)


        marker.position = geoPoint
        marker.icon = ContextCompat.getDrawable(this, R.drawable.pin)
        marker.title = "Test Marker"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        map.overlays.clear()
        map.overlays.add(marker)
        map.invalidate()
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        event?.zoomLevel?.let { controller.setZoom(it) }
        /*Log.e("TAG1", "onZoom zoom level: ${event?.zoomLevel}   source:  ${event?.source}")*/
        return false;
    }

    override fun onGpsStatusChanged(event: Int) {

    }


    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        var i = 0
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i])
            i++
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<out String>) {
        val permissionsToRequest = ArrayList<String>()
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permissionsToRequest[0]),
                REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }
}