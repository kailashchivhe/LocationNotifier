package com.kai.locationnotifier.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kai.locationnotifier.MainActivity

class MonitoringReceiver : ResultReceiver{
    companion object{
        val TAG = "MonitoringReceiver"
    }

    var mLocalBroadcastManager:LocalBroadcastManager ?= null

    constructor(handler: Handler,context: Context):super(handler){
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context)
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        super.onReceiveResult(resultCode, resultData)
        if(resultCode == 1){
            val intent = Intent()
            intent.action = MainActivity.LAUNCH_NOTIFICATION
            mLocalBroadcastManager?.sendBroadcast(intent)
        }
    }
}