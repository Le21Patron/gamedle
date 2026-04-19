package com.sefa.loldle_karakter.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LolLoreViewModel(
    private val repository: LolLoreRepository = LolLoreRepository()
) : ViewModel() {

    val champions = mutableStateListOf<ChampionSummary>()
    val isLoadingList = mutableStateOf(false)
    val isLoadingDetail = mutableStateOf(false)
    val selectedChampion = mutableStateOf<ChampionSummary?>(null)
    val selectedChampionDetail = mutableStateOf<ChampionDetail?>(null)
    val errorMessage = mutableStateOf<String?>(null)

    fun loadChampionsIfNeeded() {
        if (champions.isNotEmpty() || isLoadingList.value) return
        loadChampions()
    }

    private fun loadChampions() {
        isLoadingList.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val list = repository.fetchChampionList()
                champions.clear()
                champions.addAll(list)
            } catch (t: Throwable) {
                errorMessage.value = t.message ?: "Şampiyon listesi yüklenemedi."
            } finally {
                isLoadingList.value = false
            }
        }
    }

    fun onChampionSelected(champion: ChampionSummary) {
        selectedChampion.value = champion
        selectedChampionDetail.value = null
        loadChampionDetail(champion.id)
    }

    private fun loadChampionDetail(id: String) {
        isLoadingDetail.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val detail = repository.fetchChampionDetail(id)
                selectedChampionDetail.value = detail
            } catch (t: Throwable) {
                errorMessage.value = t.message ?: "Şampiyon detayı yüklenemedi."
            } finally {
                isLoadingDetail.value = false
            }
        }
    }

    fun clearError() {
        errorMessage.value = null
    }
}

