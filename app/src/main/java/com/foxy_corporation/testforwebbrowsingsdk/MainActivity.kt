package com.foxy_corporation.testforwebbrowsingsdk

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.foxy_corporation.webbrowsingsdk.mvp.view.WebBrowsingSDK

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        WebBrowsingSDK.initialize(this@MainActivity)
    }
}
