package com.example.shoestore.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.shoestore.presentation.home.UserPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_ai_prefs", Context.MODE_PRIVATE)

    fun saveUserPreference(pref: UserPreference) {
        prefs.edit().apply {
            putString("fav_brands", pref.favoriteBrands.joinToString(","))
            putBoolean("price_sensitive", pref.isPriceSensitive)
            remove("fav_colors") // Xóa dữ liệu rác cũ
            putLong("last_updated", System.currentTimeMillis())
            apply()
        }
    }

    fun getUserPreference(): UserPreference? {
        val brandsStr = prefs.getString("fav_brands", "") ?: ""
        if (brandsStr.isEmpty()) return null

        val brands = brandsStr.split(",").filter { it.isNotEmpty() }
        val priceSensitive = prefs.getBoolean("price_sensitive", false)

        return UserPreference(brands, priceSensitive)
    }

    fun isOutdated(): Boolean {
        val lastUpdate = prefs.getLong("last_updated", 0)
        // 3 ngày
        val timeout = 3 * 24 * 60 * 60 * 1000L
        return (System.currentTimeMillis() - lastUpdate) > timeout
    }
}