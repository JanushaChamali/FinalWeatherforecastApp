package lk.nibm.weatherforecsatapp.mvvm

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import lk.nibm.weatherforecsatapp.WeatherList
import lk.nibm.weatherforecsatapp.MyApplication
import lk.nibm.weatherforecsatapp.SharePrefs

import lk.nibm.weatherforecsatapp.service.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.abs
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class WeatherVm : ViewModel() {
    val todayWeatherLiveData = MutableLiveData<List<WeatherList>>()
    val forecastWeatherLiveData = MutableLiveData<List<WeatherList>>()
    val closetorexactlysameweatherdate = MutableLiveData<WeatherList?>()
    val cityName = MutableLiveData<String>()
    val sunrise = MutableLiveData<String>()
    val sunset = MutableLiveData<String>()
    val currentTimeZOne = MutableLiveData<String>()
    val context = MyApplication.instance


    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeather(city: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        val todayWeatherList = mutableListOf<WeatherList>()
        val currentDateTime = LocalDateTime.now()
        val currentDatePattern = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val sharePrefs = SharePrefs.getInstance(context)
        val lat = sharePrefs.getValue("lat").toString()
        val lon = sharePrefs.getValue("lon").toString()

        Log.e("CODE VM", "$lat $lon")

        val call = if (city != null) {
            RetrofitInstance.api.getWeatherByCity(city)

        } else {
            RetrofitInstance.api.getCurrentWeather(lat, lon)
        }
        val response = call.execute()

        if (response.isSuccessful) {
            sharePrefs.setValue("city", response.body()?.city?.name!!)
            val weatherList = response.body()?.weatherList
            cityName.postValue(response.body()?.city!!.name)
            response.body()?.city!!.timezone
            val id = response.body()?.city!!.timezone
            try {
                val unixTimestamp: Int? = response.body()?.city!!.sunrise
                val instant = unixTimestamp?.toLong()?.let { Instant.ofEpochSecond(it) }
                // Convert to a specific time zone
                //val zoneOffset = ZoneOffset.ofTotalSeconds(timezoneOffsetSeconds)
                val timeZoneTx = ZoneId.ofOffset("UTC", id?.let { ZoneOffset.ofTotalSeconds(it) })
                val zoneOffset = id?.let { ZoneOffset.ofTotalSeconds(it) }
                val localDateTime = instant?.let { LocalDateTime.ofInstant(it, zoneOffset) }
                // Format the time with timezone offset
                val formattedTimeWithTimeZone = localDateTime?.let {
                    if (zoneOffset != null) {
                        sunrise.postValue(formatTimeWithTimeZone(it, zoneOffset))
                        currentTimeZOne.postValue(timeZoneTx.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e("sunrise", "Error")
            }

            try {
                val unixTimestamp: Int? = response.body()?.city!!.sunset
                val instant = unixTimestamp?.toLong()?.let { Instant.ofEpochSecond(it) }
                // Convert to a specific time zone
                //val zoneOffset = ZoneOffset.ofTotalSeconds(timezoneOffsetSeconds)
                val zoneOffset = id?.let { ZoneOffset.ofTotalSeconds(it) }
                val localDateTime = instant?.let { LocalDateTime.ofInstant(it, zoneOffset) }
                // Format the time with timezone offset
                val formattedTimeWithTimeZone = localDateTime?.let {
                    if (zoneOffset != null) {
                        sunset.postValue(formatTimeWithTimeZone(it, zoneOffset))
                    }
                }
            } catch (e: Exception) {
                Log.e("sunset", "Error")
            }


            val presentDate = currentDatePattern
            weatherList?.forEach { weather ->
                val dt = weather.dtTxt
                // Separate all the weather object that has the date of today
                if (weather.dtTxt!!.split("\\s".toRegex()).contains(presentDate)) {
                    todayWeatherList.add(weather)
                }
            }

            // if the API Time closet to the system's time display that
            //if API time matches the system time also display that
            val closestWeather = findClosestWeather(todayWeatherList)
            closetorexactlysameweatherdate.postValue(closestWeather)
            todayWeatherLiveData.postValue(todayWeatherList)


        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return dateTime.format(formatter)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTimeWithTimeZone(dateTime: LocalDateTime, zoneOffset: ZoneOffset): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return dateTime.atOffset(zoneOffset).format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getForecastUpcoming(city: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        val forecastWeatherList = mutableListOf<WeatherList>()
        val currentDateTime = LocalDateTime.now()
        val currentDatePattern = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val sharePrefs = SharePrefs.getInstance(context)
        val lat = sharePrefs.getValue("lat").toString()
        val lon = sharePrefs.getValue("lon").toString()

        val call = if (city != null) {
            RetrofitInstance.api.getWeatherByCity(city)

        } else {
            RetrofitInstance.api.getCurrentWeather(lat, lon)
        }

        val response = call.execute()

        if (response.isSuccessful) {
            val weatherList = response.body()?.weatherList
            cityName.postValue(response.body()?.city!!.name)

            val presentDate = currentDatePattern

            weatherList?.forEach { weather ->
                if (!weather.dtTxt!!.split("\\s".toRegex()).contains(presentDate)) {

                    if (weather.dtTxt!!.substring(11, 16) == "12:00") {
                        forecastWeatherList.add(weather)
                    }
                }
            }

            forecastWeatherLiveData.postValue(forecastWeatherList)


        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun findClosestWeather(weatherList: List<WeatherList>): WeatherList? {
        val systemTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        var closestWeather: WeatherList? = null
        var minTimeDifference = Int.MAX_VALUE

        for (weather in weatherList) {
            val weatherTime = weather.dtTxt!!.substring(11, 16)
            val timeDifference = abs(timeToMinutes(weatherTime) - timeToMinutes(systemTime))
            if (timeDifference < minTimeDifference) {
                minTimeDifference = timeDifference
                closestWeather = weather
            }
        }
        return closestWeather
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
}