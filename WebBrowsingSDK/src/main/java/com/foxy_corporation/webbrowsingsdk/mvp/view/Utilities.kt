package com.foxy_corporation.webbrowsingsdk.mvp.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import java.lang.reflect.InvocationTargetException

class Utilities {
    fun getDeviceCountryCode(context: Context): String {
        var countryCode: String?

        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (tm != null) {
            countryCode = tm.simCountryIso
            if (countryCode != null && countryCode.length == 2)
                return countryCode.toLowerCase()
            if (tm.phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                countryCode = getCDMACountryIso()
            }
            else {
                countryCode = tm.networkCountryIso
            }
            if (countryCode != null && countryCode.length == 2)
                return countryCode.toLowerCase()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            countryCode = context.resources.configuration.locales.get(0).country
        }
        else {
            countryCode = context.resources.configuration.locale.country
        }

        return if (countryCode != null && countryCode!!.length == 2) countryCode!!.toLowerCase() else "us"
    }

    @SuppressLint("PrivateApi")
    private fun getCDMACountryIso(): String? {
        try {
            /// try to get country code from SystemProperties private class
            val systemProperties = Class.forName("android.os.SystemProperties")
            val get = systemProperties.getMethod("get", String::class.java)

            /// get homeOperator that contain MCC + MNC
            val homeOperator = get.invoke(
                systemProperties,
                "ro.cdma.home.operator.numeric"
            ) as String

            /// first 3 chars (MCC) from homeOperator represents the country code
            val mcc = Integer.parseInt(homeOperator.substring(0, 3))

            /// mapping just countries that actually use CDMA networks
            when (mcc) {
                330 -> return "PR"
                310 -> return "US"
                311 -> return "US"
                312 -> return "US"
                316 -> return "US"
                283 -> return "AM"
                460 -> return "CN"
                455 -> return "MO"
                414 -> return "MM"
                619 -> return "SL"
                450 -> return "KR"
                634 -> return "SD"
                434 -> return "UZ"
                232 -> return "AT"
                204 -> return "NL"
                262 -> return "DE"
                247 -> return "LV"
                255 -> return "UA"
            }
        } catch (ignored: ClassNotFoundException) {
        } catch (ignored: NoSuchMethodException) {
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: InvocationTargetException) {
        } catch (ignored: NullPointerException) {
        }

        return null
    }
}