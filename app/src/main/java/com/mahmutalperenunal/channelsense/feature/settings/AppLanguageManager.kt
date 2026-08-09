package com.mahmutalperenunal.channelsense.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mahmutalperenunal.channelsense.feature.settings.model.AppLanguage

object AppLanguageManager {
    fun current(): AppLanguage {
        val tag = AppCompatDelegate.getApplicationLocales()[0]?.language.orEmpty()
        return when (tag) {
            AppLanguage.TURKISH.languageTag -> AppLanguage.TURKISH
            AppLanguage.ENGLISH.languageTag -> AppLanguage.ENGLISH
            else -> AppLanguage.SYSTEM
        }
    }

    fun apply(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            if (language == AppLanguage.SYSTEM) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(language.languageTag)
        )
    }
}
