package com.example.apiapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apiapp.data.model.Character
import com.example.apiapp.ui.theme.StatusAlive
import com.example.apiapp.ui.theme.StatusDead
import com.example.apiapp.ui.theme.StatusUnknown
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterScreen(
    viewModel: CharacterViewModel,
    onCharacterClick: (Int) -> Unit,
    onCompareRequested: (Int, Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val allCharacters by viewModel.allCharacters.collectAsState()
    val lastSyncedAt by viewModel.lastSyncedAt.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val favoritesOnly by viewModel.favoritesOnly.collectAsState()

    var menuExpanded by remember { mutableStateOf(false) }
    var compareMode by remember { mutableStateOf(false) }
    var compareSelection by remember { mutableStateOf(emptyList<Int>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (compareMode) {
                CompareModeBar(
                    selectedCount = compareSelection.size,
                    onCompare = {
                        onCompareRequested(compareSelection[0], compareSelection[1])
                        compareMode = false
                        compareSelection = emptyList()
                    },
                    onCancel = {
                        compareMode = false
                        compareSelection = emptyList()
                    }
                )
            }
        },
        topBar = {
            Column {
                Box(
                    modifier = Modifier.background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    )
                ) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "PORTAL DEX",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Personajes",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        actions = {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Más opciones",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Actualizar ahora") },
                                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.loadInitial()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sincronizar en segundo plano") },
                                    leadingIcon = { Icon(Icons.Default.Send, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.syncInBackground()
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Sincronización en segundo plano programada")
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (compareMode) "Cancelar comparación" else "Comparar personajes") },
                                    onClick = {
                                        menuExpanded = false
                                        compareMode = !compareMode
                                        compareSelection = emptyList()
                                    }
                                )
                            }
                        }
                    )
                }

                SyncStatusBar(
                    isConnected = isConnected,
                    lastSyncedAt = lastSyncedAt,
                    cachedCount = allCharacters.size
                )

                AnimatedVisibility(visible = !isConnected) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sin conexión: mostrando datos guardados localmente",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                SearchAndFilterBar(
                    query = searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    selectedStatus = statusFilter,
                    onStatusSelected = viewModel::updateStatusFilter,
                    favoritesOnly = favoritesOnly,
                    onToggleFavoritesOnly = { viewModel.toggleFavoritesOnly() }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(targetState = uiState, label = "CharacterScreenState") { state ->
                when (state) {
                    is UiState.Loading -> LoadingScreen()
                    is UiState.Success -> CharacterList(
                        characters = state.characters,
                        hasMore = state.hasMore,
                        isLoadingMore = state.isLoadingMore,
                        onLoadMore = { viewModel.loadMoreCharacters() },
                        onSelectCharacter = { onCharacterClick(it.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        compareMode = compareMode,
                        compareSelection = compareSelection,
                        onToggleCompareSelection = { id ->
                            compareSelection = when {
                                compareSelection.contains(id) -> compareSelection - id
                                compareSelection.size < 2 -> compareSelection + id
                                else -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Ya seleccionaste 2 personajes, quita uno primero")
                                    }
                                    compareSelection
                                }
                            }
                        }
                    )
                    is UiState.Error -> ErrorScreen(message = state.message) { viewModel.loadInitial() }
                }
            }
        }
    }
}

@Composable
fun CompareModeBar(selectedCount: Int, onCompare: () -> Unit, onCancel: () -> Unit) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Selecciona 2 personajes ($selectedCount/2)", style = MaterialTheme.typography.bodyMedium)
            Row {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onCompare, enabled = selectedCount == 2) { Text("Comparar") }
            }
        }
    }
}

@Composable
fun SyncStatusBar(isConnected: Boolean, lastSyncedAt: Long?, cachedCount: Int) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000)
            now = System.currentTimeMillis()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) StatusAlive else StatusDead)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isConnected) "En línea" else "Sin conexión",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "SYNC ${formatRelativeSync(lastSyncedAt, now)}",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = com.example.apiapp.ui.theme.ReadoutFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$cachedCount en cache",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatRelativeSync(lastSyncedAt: Long?, now: Long): String {
    if (lastSyncedAt == null) return "nunca"
    val minutes = (now - lastSyncedAt) / 60_000
    return when {
        minutes < 1 -> "hace instantes"
        minutes < 60 -> "hace $minutes min"
        else -> "hace ${minutes / 60} h"
    }
}

@Composable
fun SearchAndFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedStatus: StatusFilter,
    onStatusSelected: (StatusFilter) -> Unit,
    favoritesOnly: Boolean,
    onToggleFavoritesOnly: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar personaje...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        ) {
            StatusFilter.entries.forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onStatusSelected(status) },
                    label = { Text(status.label) }
                )
            }
            FilterChip(
                selected = favoritesOnly,
                onClick = onToggleFavoritesOnly,
                leadingIcon = {
                    Icon(
                        imageVector = if (favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = { Text("Favoritos") }
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Abriendo un portal...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Ups! Se cerró el portal.",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, shape = MaterialTheme.shapes.medium) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reintentar")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@Composable
fun CharacterList(
    characters: List<Character>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onSelectCharacter: (Character) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    compareMode: Boolean = false,
    compareSelection: List<Int> = emptyList(),
    onToggleCompareSelection: (Int) -> Unit = {}
) {
    if (characters.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Ni en esta dimensión hay resultados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(characters, key = { it.id }) { character ->
            CharacterCard(
                character = character,
                onClick = {
                    if (compareMode) onToggleCompareSelection(character.id) else onSelectCharacter(character)
                },
                onToggleFavorite = { onToggleFavorite(character.id) },
                compareMode = compareMode,
                isSelectedForCompare = character.id in compareSelection
            )
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoadingMore -> CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    hasMore -> Button(onClick = onLoadMore) { Text("Cargar más personajes") }
                    else -> Text(
                        text = "No hay más personajes por cargar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterCard(
    character: Character,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    compareMode: Boolean = false,
    isSelectedForCompare: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectedForCompare) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = character.imageUrl,
                contentDescription = character.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .border(2.dp, statusColor(character.status), MaterialTheme.shapes.medium)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${character.species} · ${character.locationName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatusBadge(status = character.status)
            }

            if (compareMode) {
                Checkbox(checked = isSelectedForCompare, onCheckedChange = { onClick() })
            } else {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (character.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (character.isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (character.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val color = statusColor(status)
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.4f)), CircleShape)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

fun statusColor(status: String): Color = when (status.lowercase()) {
    "alive" -> StatusAlive
    "dead" -> StatusDead
    else -> StatusUnknown
}
