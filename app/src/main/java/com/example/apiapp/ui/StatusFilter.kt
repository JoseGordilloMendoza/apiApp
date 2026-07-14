package com.example.apiapp.ui

enum class StatusFilter(val label: String, val apiValue: String?) {
    ALL("Todos", null),
    ALIVE("Vivo", "Alive"),
    DEAD("Muerto", "Dead"),
    UNKNOWN("Desconocido", "unknown")
}
