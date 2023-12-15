package lk.nibm.weatherforecsatapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import lk.nibm.weatherforecsatapp.adapter.WeatherToday
import lk.nibm.weatherforecsatapp.databinding.ActivityMoreInfoBinding
import lk.nibm.weatherforecsatapp.mvvm.WeatherVm
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class MoreInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoreInfoBinding
    private lateinit var vm: WeatherVm
    private lateinit var adapter: WeatherToday
    val context = MyApplication.instance

    //Weather Today Data bind
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_info)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_more_info)
        vm = ViewModelProvider(this).get(WeatherVm::class.java)

        adapter = WeatherToday()

        binding.lifecycleOwner = this
        binding.vm=vm


        val sharePrefs = SharePrefs.getInstance(context)
        val city = sharePrefs.getValue("city").toString()
        Log.e("MoreInfo City", "$city")



        vm.getWeather(city)

        //whenever app runs clea the city that we had search previously
        val sharePrefs1 = SharePrefs.getInstance(this@MoreInfoActivity)
        sharePrefs.clearCityValue()



        vm.closetorexactlysameweatherdate.observe(this, Observer {
            val temperatureFahrenheit = it!!.main?.temp
            val temperatureCelsius = (temperatureFahrenheit?.minus(273.15))
            val temperatureFormatted = String.format("%.2f", temperatureCelsius)

            for (i in it.weather) {
                binding.descMain.text = i.description
            }
            binding.tempMain.text = "$temperatureFormatted °C"

            //binding.humidityMain.text = it.main!!.humidity.toString()
            binding.windspeed.text = it.wind?.speed.toString() + " m/s"
            val directionText = it.wind?.deg?.let { it1 -> windDirection(it1) }

            binding.windsDeg.text = directionText
            binding.windGust.text = it.wind?.gust.toString() + " m/s"


            // Temp Bind
            binding.minTemp.text = String.format("%.2f", ((it.main?.tempMin)?.minus(273.15))) + "°C"
            binding.mainTemp.text = "$temperatureFormatted °C"
            binding.maxTemp.text = String.format("%.2f", ((it.main?.tempMax)?.minus(273.15))) + "°C"

            //humidity pressure visibility
            binding.mainHumidity.text = it.main!!.humidity.toString() + "g.m-3"
            binding.mainPressure.text = it.main!!.pressure.toString() + "hPa"
            binding.mainVisibility.text = it.visibility.toString() + "m"


            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val date = inputFormat.parse(it.dtTxt!!)
                val outputFormat = SimpleDateFormat("d MMMM EEEE", Locale.getDefault())
                val dateanddayname = outputFormat.format(date!!)
                binding.dateDayMain.text = dateanddayname
                binding.chanceofrain.text = "${it.pop.toString()}%"
                binding.mainCloud.text = "${it.clouds?.all.toString()}"
                //binding.sunrise.text = vm.sunrise.toString()
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

                if (i.icon == "04d" || i.icon == "04n") {
                    binding.imageMain.setImageResource(R.drawable.fourdn)

                }

                if (i.icon == "09d" || i.icon == "09n") {
                    binding.imageMain.setImageResource(R.drawable.ninedn)

                }
                if (i.icon == "10n") {
                    binding.imageMain.setImageResource(R.drawable.moderaterain)

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

    }

    fun windDirection(angle: Int): String {
        // Define directional sectors and their corresponding angles
        val directions = mapOf(
            "N" to (348..360) + (0..11),
            "NNE" to (11..33),
            "NE" to (33..56),
            "ENE" to (56..78),
            "E" to (78..101),
            "ESE" to (101..123),
            "SE" to (123..146),
            "SSE" to (146..168),
            "S" to (168..191),
            "SSW" to (191..213),
            "SW" to (213..236),
            "WSW" to (236..258),
            "W" to (258..281),
            "WNW" to (281..303),
            "NW" to (303..326),
            "NNW" to (326..348)
        )

        // Iterate through directions and return the corresponding text
        for ((key, value) in directions) {
            if (angle in value) {
                return key
            }
        }

        // Handle angles outside the standard range
        return "Unknown"
    }


}