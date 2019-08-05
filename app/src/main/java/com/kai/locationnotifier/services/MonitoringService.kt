package com.kai.locationnotifier.services

import android.Manifest
import android.app.IntentService
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.kai.locationnotifier.MainActivity
import com.kai.locationnotifier.model.DistanceManagerSingleton
import com.kai.locationnotifier.model.PlacesManagerSingleton
import java.util.*

class MonitoringService : IntentService {

    companion object {
        val TAG = "MonitoringService"
    }

    constructor() : super(TAG)

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val resultReceiver: ResultReceiver = intent?.getParcelableExtra(MonitoringReceiver.TAG)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val request = FindCurrentPlaceRequest.newInstance(
                    Arrays.asList(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG
                    )
                )
                val placesResponse = PlacesManagerSingleton?.placesManager?.mPlacesClient?.findCurrentPlace(request)
                placesResponse?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val response = it.getResult()
                        var mCurrentPlace: Place? = null
                        var maxLikelihood: Double = 0.0
                        for (place in response!!.getPlaceLikelihoods()) {
                            Log.d(MainActivity.TAG, place.getPlace().name)
                            if (maxLikelihood < place.getLikelihood()) {
                                maxLikelihood = place.getLikelihood()
                                mCurrentPlace = place.getPlace()
                            }
                        }
                        val dist = DistanceManagerSingleton.DistanceManager?.getDistance(mCurrentPlace)
                        if (dist != null) {
                            if (dist <= 1) {
                                resultReceiver.send(1, Bundle())
                            }
                        }
                    }
                }
            }
        }
    }
}