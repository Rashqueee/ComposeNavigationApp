package com.example.composenavigationapp.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class ItemViewModel : ViewModel() {
    // List item global
    val items = mutableStateListOf<Pair<String, String>>(
        "A01" to "Judul Item 1",
        "A02" to "Judul Item 2",
        "A03" to "Judul Item 3",
        "A04" to "Judul Item 4",
        "A05" to "Judul Item 5"
    )

    fun addItem(id: String, title: String) {
        items.add(id to title)
    }
}
