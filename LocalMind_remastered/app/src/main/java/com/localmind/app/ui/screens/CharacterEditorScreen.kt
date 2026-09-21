package com.localmind.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localmind.app.domain.model.Persona
import com.localmind.app.domain.model.PersonaContextMode
import com.localmind.app.ui.theme.NeonAccent
import com.localmind.app.ui.theme.NeonBackground
import com.localmind.app.ui.theme.NeonPrimary
import com.localmind.app.ui.theme.NeonSurface
import com.localmind.app.ui.theme.NeonTextTertiary
import com.localmind.app.ui.viewmodel.CharacterEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterEditorScreen(
    personaId: String?,
    onNavigateBack: () -> Unit,
    viewModel: CharacterEditorViewModel = hiltViewModel()
) {
    val persona by viewModel.persona.collectAsState()
    val models by viewModel.models.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    // Load existing persona if editing
    LaunchedEffect(personaId) {
        if (!personaId.isNullOrBlank()) viewModel.loadPersona(personaId)
    }

    // Navigate back after save
    LaunchedEffect(isSaved) {
        if (isSaved) onNavigateBack()
    }

    val p = persona

    // Local UI state
    var name by remember(p) { mutableStateOf(p?.name ?: "") }
    var selectedIcon by remember(p) { mutableStateOf(p?.icon ?: "🤖") }
    var systemPrompt by remember(p) { mutableStateOf(p?.systemPrompt ?: "") }
    var firstMessage by remember(p) { mutableStateOf(p?.firstMessage ?: "") }
    var personality by remember(p) { mutableStateOf(p?.personality ?: "") }
    var scenario by remember(p) { mutableStateOf(p?.scenario ?: "") }
    var exampleMessages by remember(p) { mutableStateOf(p?.exampleMessages ?: "") }
    var alternateGreetings by remember(p) { mutableStateOf(p?.alternateGreetings?.toMutableList() ?: mutableListOf()) }
    var tags by remember(p) { mutableStateOf(p?.tags?.toMutableList() ?: mutableListOf()) }
    var preferredModelId by remember(p) { mutableStateOf(p?.preferredModelId) }
    var contextMode by remember(p) { mutableStateOf(p?.contextMode ?: PersonaContextMode.NONE) }

    var newTagText by remember { mutableStateOf("") }
    var newGreetingText by remember { mutableStateOf("") }
    var showAddGreeting by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    val icons = listOf("🤖", "👨‍🏫", "👩‍⚕️", "🕵️", "👩‍🍳", "🧙", "🎨", "🎵", "🎮", "🚀",
                       "🦁", "🐉", "⚔️", "🧪", "🌙", "👑", "🔮", "🌺")

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonPrimary,
        unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = NeonPrimary,
        unfocusedLabelColor = NeonTextTertiary,
        cursorColor = NeonPrimary
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (personaId.isNullOrBlank()) "New Character" else "Character Editor",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NeonPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.savePersona(
                                existingId = p?.id,
                                name = name.trim(),
                                icon = selectedIcon,
                                systemPrompt = systemPrompt.trim(),
                                firstMessage = firstMessage.trim(),
                                personality = personality.trim(),
                                scenario = scenario.trim(),
                                exampleMessages = exampleMessages.trim(),
                                alternateGreetings = alternateGreetings.toList(),
                                tags = tags.toList(),
                                preferredModelId = preferredModelId,
                                contextMode = contextMode
                            )
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = NeonPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = NeonSurface)
            )
        },
        containerColor = NeonBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── Avatar + Name ──────────────────────────────────────
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NeonSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Big icon display
                            Surface(
                                shape = CircleShape,
                                color = NeonPrimary.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(2.dp, NeonPrimary, CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(selectedIcon, fontSize = 36.sp)
                                }
                            }

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Name") },
                                modifier = Modifier.weight(1f),
                                colors = fieldColors,
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // Icon picker
                        Text("Icon", style = MaterialTheme.typography.labelLarge, color = NeonTextTertiary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(icons) { icon ->
                                Text(
                                    text = icon,
                                    fontSize = 26.sp,
                                    modifier = Modifier
                                        .clickable { selectedIcon = icon }
                                        .background(
                                            if (selectedIcon == icon) NeonPrimary.copy(alpha = 0.25f)
                                            else Color.Transparent,
                                            CircleShape
                                        )
                                        .padding(6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── System Instructions ────────────────────────────────
            item {
                SectionCard(title = "System Instructions") {
                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = { systemPrompt = it },
                        label = { Text("System Prompt") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // ── Personality ────────────────────────────────────────
            item {
                SectionCard(title = "Personality") {
                    OutlinedTextField(
                        value = personality,
                        onValueChange = { personality = it },
                        label = { Text("Personality traits, tone, style...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // ── First Message ──────────────────────────────────────
            item {
                SectionCard(title = "First Message") {
                    OutlinedTextField(
                        value = firstMessage,
                        onValueChange = { firstMessage = it },
                        label = { Text("Opening message when chat starts") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // ── Alternate Greetings ────────────────────────────────
            item {
                SectionCard(
                    title = "Alternate Greetings",
                    trailingAction = {
                        IconButton(onClick = { showAddGreeting = !showAddGreeting }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Greeting", tint = NeonPrimary)
                        }
                    }
                ) {
                    if (alternateGreetings.isEmpty() && !showAddGreeting) {
                        Text(
                            "No Alternate Greetings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonTextTertiary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    alternateGreetings.forEachIndexed { index, greeting ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = greeting,
                                onValueChange = { updated ->
                                    alternateGreetings = alternateGreetings.toMutableList().also { it[index] = updated }
                                },
                                label = { Text("Greeting ${index + 1}") },
                                modifier = Modifier.weight(1f),
                                minLines = 2,
                                colors = fieldColors,
                                shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(
                                onClick = {
                                    alternateGreetings = alternateGreetings.toMutableList().also { it.removeAt(index) }
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White.copy(alpha = 0.4f))
                            }
                        }
                    }

                    if (showAddGreeting) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newGreetingText,
                                onValueChange = { newGreetingText = it },
                                label = { Text("New greeting...") },
                                modifier = Modifier.weight(1f),
                                minLines = 2,
                                colors = fieldColors,
                                shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (newGreetingText.isNotBlank()) {
                                        alternateGreetings = alternateGreetings.toMutableList().also { it.add(newGreetingText.trim()) }
                                        newGreetingText = ""
                                        showAddGreeting = false
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = NeonPrimary)
                            }
                        }
                    }
                }
            }

            // ── Scenario ───────────────────────────────────────────
            item {
                SectionCard(title = "Scenario") {
                    OutlinedTextField(
                        value = scenario,
                        onValueChange = { scenario = it },
                        label = { Text("World, setting, context...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // ── Example Messages ───────────────────────────────────
            item {
                SectionCard(title = "Example Messages") {
                    OutlinedTextField(
                        value = exampleMessages,
                        onValueChange = { exampleMessages = it },
                        label = { Text("<START>\\nUser: ...\\nAssistant: ...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // ── Tags ───────────────────────────────────────────────
            item {
                SectionCard(title = "Tags") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTagText,
                            onValueChange = { newTagText = it },
                            label = { Text("Enter value...") },
                            modifier = Modifier.weight(1f),
                            colors = fieldColors,
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val tag = newTagText.trim()
                                if (tag.isNotBlank() && tag !in tags) {
                                    tags = tags.toMutableList().also { it.add(tag) }
                                    newTagText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPrimary),
                            shape = RoundedCornerShape(12.dp),
                            enabled = newTagText.isNotBlank()
                        ) {
                            Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonAccent.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable {
                                        tags = tags.toMutableList().also { it.remove(tag) }
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("#$tag", style = MaterialTheme.typography.labelMedium, color = NeonAccent)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Close, contentDescription = "Remove tag",
                                            tint = NeonAccent.copy(alpha = 0.7f),
                                            modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Model Binding ──────────────────────────────────────
            item {
                SectionCard(title = "Pre-bind to Model (Optional)") {
                    Box {
                        OutlinedButton(
                            onClick = { modelDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            val selectedName = models.find { it.id == preferredModelId }?.name ?: "No specific model (Default)"
                            Text(selectedName, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = modelDropdownExpanded, onDismissRequest = { modelDropdownExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("No specific model (Default)") },
                                onClick = { preferredModelId = null; modelDropdownExpanded = false }
                            )
                            models.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model.name) },
                                    onClick = { preferredModelId = model.id; modelDropdownExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // ── Context Mode ───────────────────────────────────────
            item {
                SectionCard(title = "Context Mode") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PersonaContextMode.entries.forEach { mode ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { contextMode = mode }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = contextMode == mode,
                                    onClick = { contextMode = mode },
                                    colors = RadioButtonDefaults.colors(selectedColor = NeonPrimary)
                                )
                                Column {
                                    Text(
                                        mode.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (contextMode == mode) NeonPrimary else Color.White
                                    )
                                    Text(
                                        when (mode) {
                                            PersonaContextMode.NONE -> "No conversation history sent (fastest, no memory)"
                                            PersonaContextMode.BASIC -> "Last few messages sent for context (balanced)"
                                            PersonaContextMode.FULL -> "Entire conversation sent (best coherence, slower)"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeonTextTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Save button at bottom ──────────────────────────────
            item {
                Button(
                    onClick = {
                        viewModel.savePersona(
                            existingId = p?.id,
                            name = name.trim(),
                            icon = selectedIcon,
                            systemPrompt = systemPrompt.trim(),
                            firstMessage = firstMessage.trim(),
                            personality = personality.trim(),
                            scenario = scenario.trim(),
                            exampleMessages = exampleMessages.trim(),
                            alternateGreetings = alternateGreetings.toList(),
                            tags = tags.toList(),
                            preferredModelId = preferredModelId,
                            contextMode = contextMode
                        )
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPrimary),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Character", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    trailingAction: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NeonSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                trailingAction?.invoke()
            }
            content()
        }
    }
}
