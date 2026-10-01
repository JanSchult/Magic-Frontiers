package com.example.magicfrontiers.core.model

import kotlinx.serialization.Serializable


@Serializable @JvmInline value class UnitId(val value: String)
@Serializable @JvmInline value class BuildingId(val value: String)
@Serializable @JvmInline value class FactionId(val value: String)
@Serializable @JvmInline value class PlayerId(val value: String)