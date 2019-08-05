package com.kai.locationnotifier.model

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class PlacesManager (val context: Context){
    var mPlacesClient: PlacesClient? = null
    fun init(){
        if (!Places.isInitialized()) {
            Places.initialize(context, Constants.GOOGLE_PLACES_API_KEY)
            mPlacesClient = Places.createClient(context)
        }
    }
}