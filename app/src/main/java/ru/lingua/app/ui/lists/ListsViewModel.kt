package ru.lingua.app.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lingua.app.data.WordListInfo
import ru.lingua.app.data.WordRepository
import ru.lingua.app.data.WordSource

data class ListsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val lists: List<WordListInfo> = emptyList(),
    val source: WordSource? = null,
)

/** Загружает перечень списков слов для экрана выбора. */
class ListsViewModel(private val repository: WordRepository) : ViewModel() {

    private val _state = MutableStateFlow(ListsUiState())
    val state: StateFlow<ListsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = try {
                val loaded = repository.loadLists()
                if (loaded.lists.isEmpty()) {
                    ListsUiState(isLoading = false, error = "Списков пока нет")
                } else {
                    ListsUiState(isLoading = false, lists = loaded.lists, source = loaded.source)
                }
            } catch (e: Exception) {
                ListsUiState(isLoading = false, error = "Не удалось загрузить списки: ${e.message}")
            }
        }
    }
}
