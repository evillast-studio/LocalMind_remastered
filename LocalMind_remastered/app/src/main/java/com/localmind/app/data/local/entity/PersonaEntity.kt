package com.localmind.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.localmind.app.domain.model.Persona
import java.util.UUID

@Entity(tableName = "personas")
data class PersonaEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val systemPrompt: String,
    val isDefault: Boolean,
    val preferredModelId: String? = null,
    val createdAt: Long,
    // Character Card fields (added in DB v13)
    val firstMessage: String = "",
    val personality: String = "",
    val scenario: String = "",
    val exampleMessages: String = "",
    val alternateGreetings: String = "", // JSON array stored as string
    val tags: String = ""               // JSON array stored as string
)

private fun String.toStringList(): List<String> =
    if (isBlank()) emptyList()
    else try {
        // Simple pipe-delimited storage to avoid a JSON dependency in entity layer
        split("|||").filter { it.isNotBlank() }
    } catch (e: Exception) { emptyList() }

private fun List<String>.toStoredString(): String = joinToString("|||")

fun PersonaEntity.toDomain(): Persona = Persona(
    id = id,
    name = name,
    icon = icon,
    systemPrompt = systemPrompt,
    isDefault = isDefault,
    createdAt = createdAt,
    preferredModelId = preferredModelId,
    firstMessage = firstMessage,
    personality = personality,
    scenario = scenario,
    exampleMessages = exampleMessages,
    alternateGreetings = alternateGreetings.toStringList(),
    tags = tags.toStringList()
)

fun Persona.toEntity(): PersonaEntity = PersonaEntity(
    id = id,
    name = name,
    icon = icon,
    systemPrompt = systemPrompt,
    isDefault = isDefault,
    createdAt = createdAt,
    preferredModelId = preferredModelId,
    firstMessage = firstMessage,
    personality = personality,
    scenario = scenario,
    exampleMessages = exampleMessages,
    alternateGreetings = alternateGreetings.joinToString("|||"),
    tags = tags.joinToString("|||")
)
