package com.example.magicfrontiers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.model.UnitRole
import com.example.magicfrontiers.core.model.UnitStats
import com.example.magicfrontiers.core.model.Vector2
import com.example.magicfrontiers.persistence.SaveGameRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.Test

// test/.../SaveGameRepositoryTest.kt
// Hinweis: Instrumentierter Test nötig (Context erforderlich), kein reiner JUnit-Unit-Test

@RunWith(AndroidJUnit4::class)
class SaveGameRepositoryTest {

    @Test
    fun saveAndLoadRoundTripPreservesState() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SaveGameRepository(context)

        val unit = GameUnit(
            id = UnitId("u1"), typeId = "scout", ownerId = PlayerId("p1"),
            role = UnitRole.MELEE, position = Vector2(3f, 4f), currentHealth = 60,
            stats = UnitStats(60, 8, 1f, 800L, 3f, 6f)
        )
        val original = GameState(
            tick = 42, map = emptyList(), mapWidth = 20, mapHeight = 20,
            units = mapOf(unit.id to unit), buildings = emptyMap(),
            resourceNodes = emptyMap(), playerResources = emptyMap(), factions = emptyMap()
        )

        repository.save(original, "test").getOrThrow()
        val loaded = repository.load("test").getOrThrow()

        assertEquals(original.tick, loaded.tick)
        assertEquals(original.units[unit.id]?.position, loaded.units[unit.id]?.position)

        repository.delete("test")
    }
}