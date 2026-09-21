package com.localmind.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localmind.app.data.repository.ModelRepository
import com.localmind.app.data.repository.PersonaRepository
import com.localmind.app.domain.model.Model
import com.localmind.app.domain.model.Persona
import com.localmind.app.domain.model.PersonaContextMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterEditorViewModel @Inject constructor(
    private val personaRepository: PersonaRepository,
    private val modelRepository: ModelRepository
) : ViewModel() {

    private val _persona = MutableStateFlow<Persona?>(null)
    val persona: StateFlow<Persona?> = _persona.asStateFlow()

    private val _models = MutableStateFlow<List<Model>>(emptyList())
    val models: StateFlow<List<Model>> = _models.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    init {
        viewModelScope.launch {
            modelRepository.getAllModels().collect { _models.value = it }
        }
    }

    fun loadPersona(id: String) {
        viewModelScope.launch {
            _persona.value = personaRepository.getPersonaById(id)
        }
    }

    fun savePersona(
        existingId: String?,
        name: String,
        icon: String,
        systemPrompt: String,
        firstMessage: String,
        personality: String,
        scenario: String,
        exampleMessages: String,
        alternateGreetings: List<String>,
        tags: List<String>,
        preferredModelId: String?,
        contextMode: PersonaContextMode
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val saved = Persona(
                id = existingId ?: java.util.UUID.randomUUID().toString(),
                name = name,
                icon = icon,
                systemPrompt = systemPrompt,
                firstMessage = firstMessage,
                personality = personality,
                scenario = scenario,
                exampleMessages = exampleMessages,
                alternateGreetings = alternateGreetings,
                tags = tags,
                preferredModelId = preferredModelId,
                contextMode = contextMode,
                createdAt = _persona.value?.createdAt ?: System.currentTimeMillis()
            )
            personaRepository.savePersona(saved)
            _isSaved.value = true
        }
    }
}
