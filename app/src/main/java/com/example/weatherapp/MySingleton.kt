package com.example.weatherapp

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

class MySingleton private constructor(context: Context) {
    private var requestQueue: RequestQueue? = null

    fun getRequestQueue(): RequestQueue? {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx?.applicationContext)
        }
        return requestQueue
    }
    init {
        ctx = context
        requestQueue = getRequestQueue()
    }


    fun <T> addToRequestQueue(req: Request<T>) {
        getRequestQueue()?.add(req)
    }

    companion object {
        private var instance: MySingleton? = null
        private var ctx: Context? = null

        @Synchronized
        fun getInstance(context: Context?): MySingleton? {
            if (instance == null) {
                instance = MySingleton(context!!)
            }
            return instance
        }
    }

}
