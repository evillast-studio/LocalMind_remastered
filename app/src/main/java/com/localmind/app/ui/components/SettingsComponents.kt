package com.localmind.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.localmind.app.ui.theme.*

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = NeonPrimary,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
            letterSpacing = 1.sp
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NeonElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonTextExtraMuted.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .padding(horizontal = 18.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = NeonBackground.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = NeonPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonText,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonTextSecondary
                    )
                }
            }
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                androidx.compose.material.icons.Icons.Default.ChevronRight,
                contentDescription = null,
                tint = NeonTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = NeonBackground.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (checked) NeonPrimary else NeonTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonText,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonTextSecondary
                    )
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonText,
                checkedTrackColor = NeonPrimary,
                uncheckedThumbColor = NeonTextSecondary,
                uncheckedTrackColor = NeonSurface,
                uncheckedBorderColor = NeonTextExtraMuted
            )
        )
    }
}

/**
 * Slider con recuadro de valor EDITABLE.
 *
 * - Arrastrar la barra funciona igual que antes.
 * - Tocar el recuadro naranja abre un dialogo con teclado numerico para escribir
 *   el valor exacto (los sliders con miles de pasos eran imposibles de afinar con el dedo).
 * - Si el valor escrito cae fuera de [valueRange] se ajusta al limite mas cercano.
 * - Si el slider usa pasos (steps > 0) el valor escrito NO se redondea a esos pasos:
 *   lo que escribes es lo que se guarda (por eso se puede escribir 225 en un
 *   rango 1..2048, o 1500 tokens en un rango que salta de 128 en 128).
 *
 * La firma es identica a la anterior: no hay que tocar ningun otro archivo.
 */
@Composable
fun RealTimeSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: ((Float) -> Unit)? = null,
    onValueChangeFinished: (Float) -> Unit,
    formatValue: (Float) -> String
) {
    var localValue by androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(value) }
    var showEditDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Sync external value changes if they differ significantly from localValue
    // (e.g. initial load or parent state reset)
    androidx.compose.runtime.LaunchedEffect(value) {
        if (kotlin.math.abs(localValue - value) > 0.01f) {
            localValue = value
        }
    }

    if (showEditDialog) {
        SliderValueEditDialog(
            title = title,
            currentValue = localValue,
            valueRange = valueRange,
            isInteger = isIntegerFormat(formatValue(localValue)),
            onDismiss = { showEditDialog = false },
            onConfirm = { typed ->
                localValue = typed
                onValueChange?.invoke(typed)
                onValueChangeFinished(typed)
                showEditDialog = false
            }
        )
    }

    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = NeonText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false)
            )
            Surface(
                color = NeonPrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonPrimary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showEditDialog = true }
            ) {
                Text(
                    formatValue(localValue),
                    color = NeonPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Slider(
            value = localValue.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = { newValue ->
                localValue = newValue
                onValueChange?.invoke(newValue)
            },
            onValueChangeFinished = { onValueChangeFinished(localValue) },
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = NeonPrimary,
                activeTrackColor = NeonPrimary,
                inactiveTrackColor = NeonTextExtraMuted.copy(alpha = 0.2f),
                activeTickColor = NeonPrimary.copy(alpha = 0.5f),
                inactiveTickColor = NeonTextExtraMuted.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * Un slider es "entero" si el texto que el propio slider muestra no tiene
 * separador decimal (p. ej. "225", "Batch size: 225"). Los decimales
 * (temperatura "0.70" / "0,70", font "1,0x") si lo tienen.
 * No se decide por los extremos del rango: 0..2 (temperatura) parecia entero.
 */
internal fun isIntegerFormat(formatted: String): Boolean =
    !formatted.contains('.') && !formatted.contains(',')

/**
 * Convierte lo escrito a Float. Acepta coma o punto decimal (teclado en espanol),
 * rechaza vacio, "-", "." y valores no finitos.
 */
internal fun parseSliderInput(raw: String): Float? {
    val cleaned = raw.trim().replace(',', '.')
    if (cleaned.isEmpty()) return null
    val number = cleaned.toFloatOrNull() ?: return null
    return if (number.isNaN() || number.isInfinite()) null else number
}

@Composable
private fun SliderValueEditDialog(
    title: String,
    currentValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    isInteger: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    val isIntegerRange = isInteger
    val initialText = if (isIntegerRange) {
        currentValue.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", currentValue)
    }
    var text by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(initialText) }
    val parsed = parseSliderInput(text)
    val minLabel = if (isIntegerRange) valueRange.start.toInt().toString()
        else String.format(java.util.Locale.US, "%.2f", valueRange.start)
    val maxLabel = if (isIntegerRange) valueRange.endInclusive.toInt().toString()
        else String.format(java.util.Locale.US, "%.2f", valueRange.endInclusive)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NeonSurface,
        title = { Text(title, color = NeonText, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        // Solo digitos, un separador decimal y (si el rango lo permite) signo menos
                        val allowed = input.all { it.isDigit() || it == '.' || it == ',' || it == '-' }
                        if (allowed) text = input
                    },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = if (isIntegerRange) {
                            androidx.compose.ui.text.input.KeyboardType.Number
                        } else {
                            androidx.compose.ui.text.input.KeyboardType.Decimal
                        },
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { parsed?.let { onConfirm(it.coerceIn(valueRange.start, valueRange.endInclusive)) } }
                    ),
                    isError = parsed == null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NeonText,
                        unfocusedTextColor = NeonText,
                        focusedBorderColor = NeonPrimary,
                        unfocusedBorderColor = NeonTextExtraMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Rango: $minLabel - $maxLabel",
                    color = NeonTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let { onConfirm(it.coerceIn(valueRange.start, valueRange.endInclusive)) } },
                enabled = parsed != null
            ) { Text("OK", color = NeonPrimary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = NeonTextSecondary) }
        }
    )
}
