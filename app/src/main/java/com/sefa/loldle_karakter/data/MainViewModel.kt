package com.sefa.loldle_karakter.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _isGamePanelOpen = mutableStateOf(false)
    val isGamePanelOpen: State<Boolean> = _isGamePanelOpen

    private val _isAnimePanelOpen = mutableStateOf(false)
    val isAnimePanelOpen: State<Boolean> = _isAnimePanelOpen

    fun openGamePanel() {
        _isGamePanelOpen.value = true
        _isAnimePanelOpen.value = false
    }

    fun openAnimePanel() {
        _isAnimePanelOpen.value = true
        _isGamePanelOpen.value = false
    }

    fun closePanels() {
        _isGamePanelOpen.value = false
        _isAnimePanelOpen.value = false
    }
}