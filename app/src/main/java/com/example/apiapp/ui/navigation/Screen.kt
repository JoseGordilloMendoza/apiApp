package com.example.apiapp.ui.navigation

sealed class Screen(val route: String) {
    data object CharacterList : Screen("characterList")

    data object Stats : Screen("stats")

    data object CharacterDetail : Screen("characterDetail/{characterId}") {
        const val ARG_CHARACTER_ID = "characterId"
        fun createRoute(characterId: Int) = "characterDetail/$characterId"
    }

    data object Compare : Screen("compare/{firstId}/{secondId}") {
        const val ARG_FIRST_ID = "firstId"
        const val ARG_SECOND_ID = "secondId"
        fun createRoute(firstId: Int, secondId: Int) = "compare/$firstId/$secondId"
    }
}
