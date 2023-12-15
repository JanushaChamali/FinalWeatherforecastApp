package lk.nibm.weatherforecsatapp



import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import lk.nibm.weatherforecsatapp.WeatherList
import lk.nibm.weatherforecsatapp.adapter.WeatherToday
import lk.nibm.weatherforecsatapp.databinding.ActivityMainBinding
import lk.nibm.weatherforecsatapp.mvvm.WeatherVm
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var vm: WeatherVm
    private lateinit var adapter: WeatherToday
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    //Weather Today Data bind
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        vm = ViewModelProvider(this).get(WeatherVm::class.java)

        adapter = WeatherToday()


        binding.lifecycleOwner = this
        binding.vm = vm

        vm.getWeather()

        //whenever app runs clea the city that we had search previously
        val sharePrefs = SharePrefs.getInstance(this@MainActivity)
        sharePrefs.clearCityValue()

        vm.todayWeatherLiveData.observe(this, Observer {

            val setNamelist = it as List<WeatherList>
            adapter.setList(setNamelist)
            binding.forecastRecyclerView.adapter = adapter
        })

        vm.closetorexactlysameweatherdate.observe(this, Observer {
            val temperatureFahrenheit = it!!.main?.temp
            val temperatureCelsius = (temperatureFahrenheit?.minus(273.15))
            val temperatureFormatted = String.format("%.2f", temperatureCelsius)

            for (i in it.weather) {
                binding.descMain.text = i.description
            }
            binding.tempMain.text = "$temperatureFormatted °C"

            binding.humidityMain.text = it.main!!.humidity.toString()
            binding.windspeed.text = it.wind?.speed.toString()

            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val date = inputFormat.parse(it.dtTxt!!)
                val outputFormat = SimpleDateFormat("d MMMM EEEE", Locale.getDefault())
                val dateanddayname = outputFormat.format(date!!)
                binding.dateDayMain.text = dateanddayname
                binding.chanceofrain.text = "${it.pop.toString()}%"
                Log.e("DataNow", "$temperatureFormatted, $dateanddayname, ${it.pop.toString()}")
            } catch (e: ParseException) {
                Log.e("DataNow", "Error Format")
            }



            for (i in it.weather) {
                if (i.icon == "0id") {
                    binding.imageMain.setImageResource(R.drawable.oned)
                }
                if (i.icon == "0in") {
                    binding.imageMain.setImageResource(R.drawable.onen)
                }

                if (i.icon == "02d") {
                    binding.imageMain.setImageResource(R.drawable.twod)

                }
                if (i.icon == "02n") {
                    binding.imageMain.setImageResource(R.drawable.twon)

                }

                if (i.icon == "03d" || i.icon == "03n") {
                    binding.imageMain.setImageResource(R.drawable.threedn)

                }

                if (i.icon == "10d") {
                    binding.imageMain.setImageResource(R.drawable.tend)

                }
                if (i.icon == "10n") {
                    binding.imageMain.setImageResource(R.drawable.moderaterain)

                }

                if (i.icon == "04d" || i.icon == "04n") {
                    binding.imageMain.setImageResource(R.drawable.fourdn)

                }

                if (i.icon == "09d" || i.icon == "09n") {
                    binding.imageMain.setImageResource(R.drawable.ninedn)

                }

                if (i.icon == "11d" || i.icon == "11n") {
                    binding.imageMain.setImageResource(R.drawable.elevend)

                }

                if (i.icon == "13d" || i.icon == "13n") {
                    binding.imageMain.setImageResource(R.drawable.thirteend)

                }

                if (i.icon == "50d" || i.icon == "50n") {
                    binding.imageMain.setImageResource(R.drawable.fiftydn)

                }

            }

        })


        //Location
        val getLocationButton: Button = findViewById(R.id.btnFind)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                updateLocationText(location)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }

        getLocationButton.setOnClickListener {
            requestLocationUpdates()
            vm.getWeather()
        }

        //End

//        //check for location permissions
//        if (checkLocationPermissions()) {
//            //Permissions are granded,proceed to get the current location
//            getCurrentLocation()
//
//        } else {
//            //Request location permissions
//            requestLocationPermissions()
//        }

        val searchEditText =
            binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(Color.WHITE)

        binding.next5Days.setOnClickListener {
            startActivity(Intent(this, ForeCastActivity::class.java))
        }

        binding.moreInfo.setOnClickListener {
            startActivity(Intent(this, MoreInfoActivity::class.java))
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                val prefs = SharePrefs.getInstance(this@MainActivity)
                prefs.setValue("city", query!!)

                Log.e("query", "$query")

                if (!query.isNullOrEmpty()) {
                    vm.getWeather(query)

                    binding.searchView.setQuery("", false)
                    binding.searchView.clearFocus()
                    binding.searchView.isIconified = true

                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestSingleUpdate(
                LocationManager.GPS_PROVIDER,
                locationListener,
                null
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun updateLocationText(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val text = "Location: $latitude, $longitude"
        //locationTextView.text = text
        Log.e("Location-Lat", "$latitude")
        Log.e("Location-Log", "$longitude")

        if (location != null) {

            val myprefs = SharePrefs.getInstance(this@MainActivity)
            myprefs.setValue("lon", longitude.toString())
            myprefs.setValue("lat", latitude.toString())

            Log.e("COOR Main", "$latitude $longitude")
        }
    }

    private fun checkLocationPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED

    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ), Utils.PERMISSION_REQUEST_CODE
        )

    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == Utils.PERMISSION_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                getCurrentLocation()
//            } else {
//                //permission denied handle according
//            }
//        }
//
//
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun getCurrentLocation() {
//        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        if (ActivityCompat.checkSelfPermission(
//                this, android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            val location: Location? =
//                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
//            if (location != null) {
//                val latitude = location.latitude
//                val longitude = location.longitude
//                // use the latitute and longitute values as needed
//
//                val myprefs = SharePrefs.getInstance(this@MainActivity)
//                myprefs.setValue("lon", longitude.toString())
//                myprefs.setValue("lat", latitude.toString())
//
//                Log.e("COOR Main", "$latitude $longitude")
//            }
//        }
//
//
//    }
}



