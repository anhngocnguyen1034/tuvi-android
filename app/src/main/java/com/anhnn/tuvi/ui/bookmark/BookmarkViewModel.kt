package com.anhnn.tuvi.ui.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anhnn.tuvi.data.local.BookmarkDao
import com.anhnn.tuvi.data.local.BookmarkItemEntity
import com.anhnn.tuvi.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarkViewModel(private val dao: BookmarkDao) : ViewModel() {

    val bookmarks: StateFlow<List<BookmarkItemEntity>> = dao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Long) = viewModelScope.launch { dao.deleteBookmarkById(id) }

    fun updateTitle(id: Long, newTitle: String) {
        if (newTitle.isNotBlank()) viewModelScope.launch { dao.updateTitle(id, newTitle.trim()) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BookmarkViewModel(AppContainer.bookmarkDao) as T
        }
    }
}
