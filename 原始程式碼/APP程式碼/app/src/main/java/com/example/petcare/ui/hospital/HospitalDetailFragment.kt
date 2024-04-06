package com.example.petcare.ui.hospital


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.petcare.R
import com.example.petcare.data.Feature
import com.example.petcare.databinding.FragmentHospitalDetailBinding
import com.example.petcare.databinding.FragmentPetsInfoBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class HospitalDetailFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private var _binding: FragmentHospitalDetailBinding? = null
    lateinit var model: HospitalViewModel
    lateinit var msgString: String
    lateinit var msgList: List<String>

    private lateinit var mMap: GoogleMap
    // 取得現在位置
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val permissionCode = 1

    // 目的地位置
    private var destinationLat = 0.0
    private var destinationLng = 0.0

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHospitalDetailBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model = ViewModelProvider(requireActivity())[HospitalViewModel::class.java]
        model.message.observe(viewLifecycleOwner, Observer{
            msgString = it
            msgList = msgString.split(",")
            Log.d("List", msgList.toString())
            binding.hospitalNameDetail.text = msgList[0]
            binding.phoneDetail.text = msgList[1]
            binding.addressDetail.text = msgList[2]
            if (msgList[3] == "false") {
                binding.emergencyDepartmentDetail.text = "無資料"
            } else {
                binding.emergencyDepartmentDetail.text = msgList[4]
            }
            // 目的地
            val address = binding.addressDetail.text.toString()
            val gc = Geocoder(requireContext(), Locale.getDefault())
            val addresses = gc.getFromLocationName(address, 2)
            val destination = addresses?.get(0)
            destinationLat = destination!!.latitude
            destinationLng = destination.longitude
            Log.d("目的地經緯度", "$destinationLat, $destinationLng")
        })

        // 地圖設置
        val mapFragment = (childFragmentManager.findFragmentById(R.id.map)) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 現在位置
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        binding.closeBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.DetailLayoutFragment, HospitalFragment()).commit()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->

            if (location != null){
                lastLocation = location
                Log.d("測試", "$lastLocation")
//                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarkInMap(location)
            }
        }
    }

    private fun placeMarkInMap(location: Location) {
        // 現在位置
        val currentLatLong = LatLng(location.latitude, location.longitude)
        Log.d("測試", "${location.latitude}, ${location.longitude}")
        val markerOptions = MarkerOptions().position(currentLatLong).title("現在位置")
        mMap.addMarker(markerOptions)

        // 醫院位置
        val hospitalLatLong = LatLng(destinationLat, destinationLng)
        val markerHospital = MarkerOptions().position(hospitalLatLong).title("醫院位置")
        mMap.addMarker(markerHospital)

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))

        val toHospital = "$destinationLat,$destinationLng"
        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=${lastLocation.latitude},${lastLocation.longitude}&destination=$toHospital&key="
        val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener {
                response ->
            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")
            for (i in 0 until steps.length()) {
                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                path.add(PolyUtil.decode(points))
            }
            for (i in 0 until path.size) {
                mMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }
        }, Response.ErrorListener {
        }){}
        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(directionsRequest)
    }

    override fun onMarkerClick(p0: Marker) = false

}
