package lk.nibm.weatherforecsatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import lk.nibm.weatherforecsatapp.WeatherList
import lk.nibm.weatherforecsatapp.adapter.ForeCastAdapter
import lk.nibm.weatherforecsatapp.mvvm.WeatherVm


class ForeCastActivity : AppCompatActivity() {

    private lateinit var adaptreForeCastAdapter: ForeCastAdapter
    lateinit var viM: WeatherVm
    lateinit var rvForeCast: RecyclerView

    @RequiresApi(64)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        try {



        viM = ViewModelProvider(this).get(WeatherVm::class.java)

        adaptreForeCastAdapter = ForeCastAdapter()

        rvForeCast = findViewById(R.id.rvForeCast)

        val sharePrefs = SharePrefs.getInstance(this)
        val city = sharePrefs.getValueOrNull("city")

        if (city != null) {
            viM.getForecastUpcoming(city)
        } else {
            viM.getForecastUpcoming()
        }
        viM.forecastWeatherLiveData.observe(this, Observer {
            val setNewList = it as List<WeatherList>

            Log.d("Forecast LiveData", setNewList.toString())

            adaptreForeCastAdapter.setList(setNewList)

            rvForeCast.adapter = adaptreForeCastAdapter
        })
        }
        catch (e:Exception){
            Log.e("ForeCastActivity Data","ERROR")
        }

    }
}