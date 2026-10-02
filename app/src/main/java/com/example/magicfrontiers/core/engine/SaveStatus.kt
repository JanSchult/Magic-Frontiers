package com.example.magicfrontiers.core.engine

sealed interface SaveStatus {
    data object Idle : SaveStatus
    data object Saving : SaveStatus
    data object Saved : SaveStatus
    data object Error : SaveStatus
}