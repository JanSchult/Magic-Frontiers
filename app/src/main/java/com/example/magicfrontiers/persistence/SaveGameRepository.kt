package com.example.magicfrontiers.persistence

import android.content.Context
import com.example.magicfrontiers.core.model.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class SaveGameRepository(private val context: Context) {

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true // schützt vor Abstürzen bei zukünftigen Model-Änderungen
    }

    private fun saveFile(slot: String): File =
        File(context.filesDir, "savegame_$slot.json")

    suspend fun save(state: GameState, slot: String = "autosave"): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val content = json.encodeToString(state)
                saveFile(slot).writeText(content)
            }
        }

    suspend fun load(slot: String = "autosave"): Result<GameState> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = saveFile(slot)
                require(file.exists()) { "Kein Spielstand im Slot '$slot' gefunden" }
                json.decodeFromString(file.readText())
            }
        }

    suspend fun exists(slot: String = "autosave"): Boolean =
        withContext(Dispatchers.IO) { saveFile(slot).exists() }

    suspend fun delete(slot: String = "autosave"): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { saveFile(slot).delete()
                Unit
            }
        }
}