package com.example.mindquest.data.settings

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "mindquest_settings")
