package com.kai.locationnotifier

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.kai.locationnotifier.model.*
import com.kai.locationnotifier.services.MonitoringReceiver
import com.kai.locationnotifier.services.MonitoringService
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    var mStartStopButton: Button? = null
    var mCurrentEditText: EditText? = null

    companion object {
        val TAG = "MainActivity"
        val LAUNCH_NOTIFICATION = "MainActivity.LAUNCH_NOTIFICATION"
    }
    val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action != null) {
                when (intent.action) {
                    LAUNCH_NOTIFICATION -> {
                        Log.d(TAG,"You have arrived near your location")
                        mStartStopButton?.text="STOP"
                        mStartStopButton?.isEnabled = false
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mStartStopButton = startStopButton
        mCurrentEditText = currentEditText
        initReceiver()
        initPlaces()
        initAutoComplete()
        mStartStopButton?.setOnClickListener {
            val intentService = Intent(applicationContext, MonitoringService::class.java)
            intentService.putExtra(MonitoringReceiver.TAG, MonitoringReceiver(Handler(), applicationContext))
//            val pendingIntent = PendingIntent.getService(applicationContext,111,intentService,PendingIntent.FLAG_CANCEL_CURRENT)
//            val calender = Calendar.getInstance()
//            calender.add(Calendar.SECOND,5)
//            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            alarmManager.set(AlarmManager.RTC_WAKEUP,calender.timeInMillis,pendingIntent)
            startService(intentService)
        }
    }

    fun initReceiver(){
        val intentFilter = IntentFilter()
        intentFilter.addAction(LAUNCH_NOTIFICATION)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(mBroadcastReceiver, intentFilter)
    }


    fun initPlaces() {
        PlacesManagerSingleton.placesManager = PlacesManager(applicationContext)
        PlacesManagerSingleton?.placesManager?.init()
        checkForPermissions()
    }

    fun initAutoComplete(){
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocompleteFragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.d(TAG, "" + place.name)
                mStartStopButton?.text = "START"
                mStartStopButton?.visibility = View.VISIBLE
                autocompleteFragment.setText(place.name)
                DistanceManagerSingleton.DistanceManager = DistanceManager(place,applicationContext)
            }

            override fun onError(status: Status) {
                Log.d(TAG, "" + status.statusMessage)
            }
        })
        autocompleteFragment.getView()?.findViewById<View>(R.id.places_autocomplete_clear_button)?.setOnClickListener {
            mStartStopButton?.text = ""
            mStartStopButton?.visibility = View.INVISIBLE
            autocompleteFragment.setText("")
        }
    }

    fun checkForPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            val request = FindCurrentPlaceRequest.newInstance(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,Place.Field.LAT_LNG))
            val placesResponse = PlacesManagerSingleton?.placesManager?.mPlacesClient?.findCurrentPlace(request)
            placesResponse?.addOnCompleteListener {
                if (it.isSuccessful) {
                    val response = it.getResult()
                    var mCurrentPlace:Place ?= null
                    var maxLikelihood:Double = 0.0
                    for (place in response!!.getPlaceLikelihoods()) {
                        Log.d(TAG, place.getPlace().name)
                        if (maxLikelihood < place.getLikelihood()) {
                            maxLikelihood = place.getLikelihood()
                            mCurrentPlace = place.getPlace()
                        }
                    }
                    mCurrentEditText?.setText(mCurrentPlace?.name)
                    mCurrentEditText?.isEnabled=false
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission GRANTED")
                checkForPermissions()
            } else {
                Log.d(TAG, "Permission DENIED")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(mBroadcastReceiver)
    }
}
