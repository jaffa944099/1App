package io.github.jaffa944099.oneapp

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
            MaterialTheme(colorScheme = colors) {
                TaskHub()
            }
        }
    }

    @Composable
    private fun TaskHub() {
        var preferences by remember { mutableStateOf<Preferences?>(null) }
        var storageUnavailable by remember { mutableStateOf(false) }
        var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
        var favoritesOnly by rememberSaveable { mutableStateOf(false) }
        val task = pilotTasks.find { it.id == selectedId }
        val listState = rememberLazyListState()
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            taskPreferences.data
                .catch { error ->
                    if (error !is IOException) throw error
                    storageUnavailable = true
                }
                .collect { preferences = it }
        }
        BackHandler(enabled = task != null) { selectedId = null }

        fun open(taskToOpen: PilotTask) {
            if (!openTask(taskToOpen)) {
                scope.launch { snackbar.showSnackbar(getString(R.string.browser_error)) }
            }
        }

        fun saveChange(message: Int? = null, write: suspend () -> Unit) {
            scope.launch {
                try {
                    write()
                } catch (_: IOException) {
                    snackbar.showSnackbar(getString(R.string.storage_write_error))
                    return@launch
                }
                if (message != null) snackbar.showSnackbar(getString(message))
            }
        }

        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { insets ->
            Box(
                modifier = Modifier.fillMaxSize().padding(insets),
                contentAlignment = Alignment.TopCenter,
            ) {
                val stored = preferences
                if (stored == null && !storageUnavailable) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    val favorites = stored?.get(favoriteTasksKey).orEmpty()
                    val content = Modifier.widthIn(max = 640.dp).fillMaxWidth()
                    if (task == null) {
                        TaskList(
                            modifier = content,
                            listState = listState,
                            favorites = favorites,
                            favoritesOnly = favoritesOnly,
                            storageUnavailable = storageUnavailable,
                            onFilter = { favoritesOnly = it },
                            onDetails = { selectedId = it.id },
                            onOpen = ::open,
                        )
                    } else {
                        TaskDetails(
                            modifier = content.imePadding(),
                            task = task,
                            isFavorite = task.id in favorites,
                            savedReference = stored?.get(referenceKey(task)),
                            canSave = !storageUnavailable,
                            onBack = { selectedId = null },
                            onOpen = { open(task) },
                            onFavorite = { saveChange { toggleFavorite(task) } },
                            onSave = { value ->
                                val message = if (value.isBlank()) {
                                    R.string.reference_removed
                                } else {
                                    R.string.reference_saved
                                }
                                saveChange(message) { saveReference(task, value) }
                            },
                            onCopy = { value, label ->
                                copyReference(value, label)
                                if (Build.VERSION.SDK_INT < 33) {
                                    scope.launch {
                                        snackbar.showSnackbar(getString(R.string.reference_copied))
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    private fun copyReference(value: String, label: String) {
        val clip = ClipData.newPlainText(label, value)
        if (Build.VERSION.SDK_INT >= 33) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        getSystemService(ClipboardManager::class.java).setPrimaryClip(clip)
    }
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF20574E),
    surface = Color(0xFFF7F9F4),
    background = Color(0xFFF7F9F4),
    surfaceContainerLow = Color(0xFFEFF4ED),
    onSurface = Color(0xFF172521),
    onSurfaceVariant = Color(0xFF40524C),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DD7C9),
    surface = Color(0xFF101B18),
    background = Color(0xFF101B18),
    surfaceContainerLow = Color(0xFF172622),
    onSurface = Color(0xFFE2EEE8),
    onSurfaceVariant = Color(0xFFB7CAC3),
)

@Composable
private fun TaskList(
    modifier: Modifier,
    listState: LazyListState,
    favorites: Set<String>,
    favoritesOnly: Boolean,
    storageUnavailable: Boolean,
    onFilter: (Boolean) -> Unit,
    onDetails: (PilotTask) -> Unit,
    onOpen: (PilotTask) -> Unit,
) {
    val visibleTasks = if (favoritesOnly) pilotTasks.filter { it.id in favorites } else pilotTasks
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF102D2B))) {
                Column(
                    Modifier.fillMaxWidth().padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFD8F78A),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(stringResource(R.string.pilot_region), color = Color(0xFFB7E0D6))
                    Text(stringResource(R.string.home_summary), color = Color(0xFFE2EEE8))
                }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !favoritesOnly,
                    onClick = { onFilter(false) },
                    label = { Text(stringResource(R.string.all_tasks)) },
                )
                FilterChip(
                    selected = favoritesOnly,
                    onClick = { onFilter(true) },
                    label = { Text(stringResource(R.string.favorites)) },
                    enabled = !storageUnavailable,
                )
            }
        }
        if (storageUnavailable) {
            item { Text(stringResource(R.string.storage_read_error)) }
        }
        if (visibleTasks.isEmpty() && !storageUnavailable) {
            item { Text(stringResource(R.string.no_favorites)) }
        }
        items(visibleTasks, key = { it.id }) { task ->
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        stringResource(task.provider),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        stringResource(task.title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(stringResource(task.summary), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onOpen(task) }) { Text(stringResource(R.string.open_site)) }
                        OutlinedButton(onClick = { onDetails(task) }) {
                            Text(stringResource(R.string.details))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskDetails(
    modifier: Modifier,
    task: PilotTask,
    isFavorite: Boolean,
    savedReference: String?,
    canSave: Boolean,
    onBack: () -> Unit,
    onOpen: () -> Unit,
    onFavorite: () -> Unit,
    onSave: (String) -> Unit,
    onCopy: (String, String) -> Unit,
) {
    val referenceLabel = task.referenceLabel
    var draft by rememberSaveable(task.id) { mutableStateOf(savedReference.orEmpty()) }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TextButton(onClick = onBack) { Text(stringResource(R.string.back_to_tasks)) }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(task.provider), color = MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(task.title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    task.url.toUri().host.orEmpty(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onOpen) { Text(stringResource(R.string.open_site)) }
                    OutlinedButton(onClick = onFavorite, enabled = canSave) {
                        val label = if (isFavorite) R.string.remove_favorite else R.string.add_favorite
                        Text(stringResource(label))
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        stringResource(R.string.before_you_start),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(stringResource(task.checklist))
                }
            }
        }
        if (referenceLabel != null) {
            item {
                val label = stringResource(referenceLabel)
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            stringResource(R.string.reference_heading),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it.replace("\n", "").replace("\r", "").take(80) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(label) },
                            supportingText = { Text(stringResource(R.string.reference_hint)) },
                            singleLine = true,
                            enabled = canSave,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onSave(draft) },
                                enabled = canSave && draft.trim() != savedReference.orEmpty(),
                            ) { Text(stringResource(R.string.save_reference)) }
                            if (savedReference != null) {
                                OutlinedButton(onClick = { onCopy(savedReference, label) }) {
                                    Text(stringResource(R.string.copy_reference))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
