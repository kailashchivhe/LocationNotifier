package com.kai.locationnotifier.model

import android.content.Context
import com.google.android.libraries.places.api.model.Place

class DistanceManager(val destination:Place,val context: Context){

    fun getDistance(place: Place?):Double{
        val lat2 = destination.latLng?.latitude as Double
        val lng2 = destination.latLng?.longitude as Double
        val lat1 = place?.latLng?.latitude as Double
        val lng1 = place?.latLng?.longitude as Double
        val earthRadius = 3958.75
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val sindLat = Math.sin(dLat / 2)
        val sindLng = Math.sin(dLng / 2)
        val a = Math.pow(sindLat, 2.0) + (Math.pow(sindLng, 2.0)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val dist = earthRadius * c
        return dist
    }
}