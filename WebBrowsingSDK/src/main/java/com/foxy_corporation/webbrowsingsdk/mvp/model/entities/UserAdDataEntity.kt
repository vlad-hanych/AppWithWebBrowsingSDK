package com.foxy_corporation.webbrowsingsdk.mvp.model.entities

data class UserAdDataEntity(val id: String,
                            val application: String,
                            val country: String,
                            val tz: String,
                            val os: String,
                            val device: String,
                            val deviceId: String,
                            val referrer: String)