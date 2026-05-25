package com.example.diplom.utils

import android.content.Context

class CurrentRegionManager(
    context: Context
) {

    private val prefs =

        context.getSharedPreferences(
            "current_region",
            Context.MODE_PRIVATE
        )

    fun saveRegion(
        regionName:String
    ){

        prefs.edit()
            .putString(
                "name",
                regionName
            )
            .apply()
    }

    fun getRegion():String{

        return prefs.getString(
            "name",
            "Город Якутск"
        ) ?: "Город Якутск"
    }
}