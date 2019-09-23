package com.foxy_corporation.webbrowsingsdk.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class InstallReferrerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val referrer = intent.getStringExtra("referrer")

        Log.d("WebBrowsingSDK", "InstallReferrerReceiver_referrer_$referrer")
    }
}