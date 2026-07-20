package com.example.apiapp.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.apiapp.MainActivity
import com.example.apiapp.R

/** Shortcuts dinamicos del launcher (mantener presionado el icono de la app). */
object AppShortcuts {
    const val EXTRA_DESTINATION = "shortcut_destination"
    const val DESTINATION_FAVORITES = "favorites"
    const val DESTINATION_STATS = "stats"

    fun register(context: Context) {
        val favorites = ShortcutInfoCompat.Builder(context, DESTINATION_FAVORITES)
            .setShortLabel("Favoritos")
            .setLongLabel("Ver personajes favoritos")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_favorite))
            .setIntent(destinationIntent(context, DESTINATION_FAVORITES))
            .build()

        val stats = ShortcutInfoCompat.Builder(context, DESTINATION_STATS)
            .setShortLabel("Estadísticas")
            .setLongLabel("Ver estadísticas de tu colección")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_stats))
            .setIntent(destinationIntent(context, DESTINATION_STATS))
            .build()

        ShortcutManagerCompat.setDynamicShortcuts(context, listOf(favorites, stats))
    }

    private fun destinationIntent(context: Context, destination: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(EXTRA_DESTINATION, destination)
        }
}
