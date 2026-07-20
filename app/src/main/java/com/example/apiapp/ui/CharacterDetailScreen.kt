package com.example.apiapp.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apiapp.data.model.Character
import com.example.apiapp.ui.theme.ReadoutFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    character: Character,
    episodeState: EpisodeUiState,
    onBack: () -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
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
                    title = { Text(character.name, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val shareText = "¡Mira a ${character.name} en PortalDex! " +
                                "${character.species} · ${character.status}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir personaje"))
                        }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Compartir",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = { onToggleFavorite(character.id) }) {
                            Icon(
                                imageVector = if (character.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (character.isFavorite) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = character.imageUrl,
                contentDescription = character.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(180.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .border(3.dp, statusColor(character.status), MaterialTheme.shapes.extraLarge)
            )

            Spacer(modifier = Modifier.height(20.dp))

            StatusBadge(status = character.status)

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                DetailRow(label = "Especie", value = character.species)
                if (character.type.isNotBlank()) {
                    DetailRow(label = "Subespecie", value = character.type)
                }
                DetailRow(label = "Género", value = character.gender)
                DetailRow(label = "Origen", value = character.originName)
                DetailRow(label = "Última ubicación conocida", value = character.locationName)
                EpisodesSection(episodeState, totalEpisodes = character.episodeCount)
            }
        }
    }
}

@Composable
private fun EpisodesSection(episodeState: EpisodeUiState, totalEpisodes: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(
            text = "Episodios ($totalEpisodes apariciones)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        when (episodeState) {
            is EpisodeUiState.Idle, EpisodeUiState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cargando...", style = MaterialTheme.typography.bodyMedium)
            }
            is EpisodeUiState.Success -> Column {
                episodeState.episodes.forEach { episode ->
                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                        Text(
                            text = episode.episode,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = ReadoutFontFamily,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(episode.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            is EpisodeUiState.Unavailable -> Text(
                text = "No disponible",
                style = MaterialTheme.typography.bodyLarge
            )
            is EpisodeUiState.Error -> Text(
                text = episodeState.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
    }
}
