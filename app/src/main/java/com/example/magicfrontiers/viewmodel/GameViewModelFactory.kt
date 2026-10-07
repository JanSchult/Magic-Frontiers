package com.example.magicfrontiers.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.magicfrontiers.core.engine.InitialGameStateFactory
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.persistence.SaveGameRepository

class GameViewModelFactory(
    private val playerFactionId: String,
    private val aiFactionId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            ?: error("Application context missing")
        val initialState = InitialGameStateFactory.create(
            playerFactionId = playerFactionId,
            aiFactionId = aiFactionId
        )
        @Suppress("UNCHECKED_CAST")
        return GameViewModel(
            initialState = initialState,
            saveGameRepository = SaveGameRepository(application),
            localPlayerId = PlayerId("p1")
        ) as T
    }
}