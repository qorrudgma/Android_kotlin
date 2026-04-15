package com.example.android

import android.content.Context

object ApiSettings {

    private const val PREF_NAME = "app_settings"
    private const val KEY_IP = "saved_ip"
    private const val KEY_PORT = "saved_port"

    fun saveServerInfo(
        context: Context,
        ip: String,
        port: String
    ) {
        val prefs =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putString(KEY_IP, ip)
            .putString(KEY_PORT, port)
            .apply()
    }

    fun getIp(context: Context): String {
        val prefs =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        return prefs.getString(KEY_IP, "10.0.2.2") ?: "10.0.2.2"
    }

    fun getPort(context: Context): String {
        val prefs =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        return prefs.getString(KEY_PORT, "7237") ?: "7237"
    }

    fun getBaseUrl(context: Context): String {
        val ip = getIp(context)
        val port = getPort(context)

        return "http://$ip:$port"
    }
}