package com.example.weatherapp

import android.content.Context
import android.Manifest.permission
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.textclassifier.ConversationActions
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.Response.Listener
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class MainActivity() : AppCompatActivity(),AdapterView.OnItemSelectedListener, Parcelable {
    var tvSehir:TextView? = null
    var location:SimpleLocation? = null
    var latitude :String? = null
    var longitude :String? = null
    constructor(parcel: Parcel) : this() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        setContentView(R.layout.activity_main)
        val a = AnimationUtils.loadAnimation(this, R.anim.fade1)
        var spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.sehirler,R.layout.spinnerstyle)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.background.setColorFilter(resources.getColor(R.color.bestColor),PorterDuff.Mode.SRC_ATOP)
        spinner.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        spinner.adapter = spinnerAdapter
        spinner.setTitle("Şehir Seçiniz")
        spinner.setOnItemSelectedListener(this)
        spinner.setSelection(1)
        spinner.startAnimation(a)
        verileriGetir("İstanbul")
        tvSehir?.startAnimation(a)
        tvAciklama.startAnimation(a)
        tvTarih.startAnimation(a)
        tvDerece.startAnimation(a)



    }
    private fun anlikSehirGetir(lat: String?,long:String?) {
        val url = "https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+long+"&appid=3bd9a55edcca88e2e7b6e7bc045fcf66&lang=tr&units=metric"
        var sehirAdi:String? = null
        val havaDurumuObjeRequest2 = JsonObjectRequest(Request.Method.GET,url,null,object : Listener<JSONObject>
        {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onResponse(response: JSONObject?)  {
                var main = response?.getJSONObject("main")
                var sicaklik = main?.getInt("temp")
                tvDerece.text = sicaklik.toString()
                sehirAdi = response?.getString("name")
                tvSehir?.setText(sehirAdi)
                var weather = response?.getJSONArray("weather")
                var aciklama = weather?.getJSONObject(0)?.getString("description")
                tvAciklama.text = aciklama
                var icon = weather?.getJSONObject(0)?.getString("icon")
                if(icon?.last()== 'd')
                {
                    layout.background = getDrawable(R.drawable.aday)
                    tvAciklama.setTextColor(resources.getColor(R.color.morning))
                    tvAciklama2.setTextColor(resources.getColor(R.color.morning))

                }
                else
                {

                    layout.background = getDrawable(R.drawable.night2)
                }
                var resimDosyaAdi =  resources.getIdentifier("icon_" + icon?.sonkarakterisil() , "drawable",packageName)
                imageHava.setImageResource(resimDosyaAdi)
                Log.e("mami", sicaklik.toString() + sehirAdi + aciklama + icon)
                tvTarih.text = tarihYazdir()
            }
        },object : Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {

            }
        })
        MySingleton.getInstance(this)?.addToRequestQueue(havaDurumuObjeRequest2)


    }
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        tvSehir = view as TextView
        if(position == 0)
        {
            location = SimpleLocation(this)
            if(!location!!.hasLocationEnabled())
            {
                Toast.makeText(this,"GPS aç ki yerini bulalım",Toast.LENGTH_LONG).show()
                SimpleLocation.openSettings(this)
            }
            else
            {
                if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),60)
                }
                else
                {
                    location = SimpleLocation(this)
                    latitude = String.format("%.6f",location?.latitude)
                    longitude = String.format("%.6f",location?.longitude)
                    anlikSehirGetir(latitude,longitude)
                }
            }
        }
        else
        {
            var secilenSehir = parent?.getItemAtPosition(position).toString()
            verileriGetir(secilenSehir)
        }

    }
    fun verileriGetir(sehir:String)
    {
        val url = "https://api.openweathermap.org/data/2.5/weather?q="+sehir+"&appid=3bd9a55edcca88e2e7b6e7bc045fcf66&lang=tr&units=metric"
        val havaDurumuObjeRequest = JsonObjectRequest(Request.Method.GET,url,null,object : Listener<JSONObject>
        {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onResponse(response: JSONObject?) {
                var main = response?.getJSONObject("main")
                var wind:JSONObject? = response?.getJSONObject("wind")
                var windSpeed:Double? = wind?.getDouble("speed")
                var sicaklik = main?.getInt("temp")
                var maxSicaklik:Int? = main?.getInt("temp_max")
                var minSicaklik:Int? = main?.getInt("temp_min")
                tvAciklama2.text = String.format("%.2f",donusturme(windSpeed)) + " km/s"
                tvDerece.text = sicaklik.toString()
                var sehirAdi = response?.getString("name")
                tvSehir?.setText(sehirAdi)
                var weather = response?.getJSONArray("weather")
                var aciklama = weather?.getJSONObject(0)?.getString("description")
                tvAciklama.text = aciklama
                var icon = weather?.getJSONObject(0)?.getString("icon")
                if(icon?.last()== 'd')
                {
                    layout.background = getDrawable(R.drawable.aday)
                    tvAciklama.setTextColor(resources.getColor(R.color.morning))
                    tvAciklama2.setTextColor(resources.getColor(R.color.morning))

                }
                else
                {
                    //tvAciklama.setTextColor(Color.parseColor("#C1FFFFFF"))
                    layout.background = getDrawable(R.drawable.night2)
                }
                var resimDosyaAdi =  resources.getIdentifier("icon_" + icon?.sonkarakterisil() , "drawable",packageName)
                imageHava.setImageResource(resimDosyaAdi)
                Log.e("mami", sicaklik.toString() + sehirAdi + aciklama + icon)
                tvTarih.text = tarihYazdir()
            }
        },object : Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {

            }
        })
        MySingleton.getInstance(this)?.addToRequestQueue(havaDurumuObjeRequest)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        if(requestCode == 60)
        {
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                location = SimpleLocation(this)
                latitude = String.format("%.6f",location?.latitude)
                longitude = String.format("%.6f",location?.longitude)
                anlikSehirGetir(latitude,longitude)
            }
            else
            {
                spinner.setSelection(1)
                Toast.makeText(this,"İzin alınamadı",Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }




    fun tarihYazdir():String
    {
        var takvim = Calendar.getInstance().time
        var formatlayici = SimpleDateFormat("EEEE, MMMM yyyy", Locale("tr"))
        var tarih=formatlayici.format(takvim)
        return tarih
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainActivity> {
        override fun createFromParcel(parcel: Parcel): MainActivity {
            return MainActivity(parcel)
        }

        override fun newArray(size: Int): Array<MainActivity?> {
            return arrayOfNulls(size)
        }
    }
    private fun String.sonkarakterisil(): String {
        return this.substring(0,this.length-1)
    }
    fun donusturme(meter:Double?): Double
    {
        return meter!!.times(3.6)
    }

}

