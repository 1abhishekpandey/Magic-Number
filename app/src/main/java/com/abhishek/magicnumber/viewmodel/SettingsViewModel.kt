package com.abhishek.magicnumber.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhishek.magicnumber.data.PreferencesRepository
import com.abhishek.magicnumber.model.NumberLayout
import com.abhishek.magicnumber.model.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the settings screen.
 *
 * Exposes settings as a StateFlow and provides methods to update them.
 */
class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    /**
     * Current settings as a StateFlow.
     * Updates automatically when settings change.
     */
    val settings: StateFlow<Settings> = preferencesRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    /**
     * Updates the maximum number setting.
     *
     * @param maxNumber New max number (31, 63, or 127)
     */
    fun updateMaxNumber(maxNumber: Int) {
        viewModelScope.launch {
            preferencesRepository.updateMaxNumber(maxNumber)
        }
    }

    /**
     * Updates the number layout setting.
     *
     * @param layout New layout style
     */
    fun updateNumberLayout(layout: NumberLayout) {
        viewModelScope.launch {
            preferencesRepository.updateNumberLayout(layout)
        }
    }

    companion object {
        /**
         * Factory for creating SettingsViewModel with dependencies.
         */
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = PreferencesRepository(context.applicationContext)
                    return SettingsViewModel(repository) as T
                }
            }
        }
    }
}
