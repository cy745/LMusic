package com.lalilu.lmusic.compose.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lalilu.lmusic.compose.NavigationWrapper
import com.lalilu.component.base.UiAction
import com.lalilu.component.base.UiPresenter
import com.lalilu.component.base.UiState
import com.lalilu.lmusic.viewmodel.SearchLyricViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent
import kotlin.coroutines.CoroutineContext

data class SearchLyricState(
    val mediaId: String,
    val selectedId: Int,
    val onAction: (action: UiAction) -> Unit
) : UiState

sealed class SearchLyricAction : UiAction {
    data class UpdateMediaId(val mediaId: String) : SearchLyricAction()
    data class UpdateSelectedId(val selectedId: Int) : SearchLyricAction()
    data class SaveFor(val mediaId: String, val selectedId: Int) : SearchLyricAction()
    data class SearchFor(val keywords: String) : SearchLyricAction()
}

object SearchLyricPresenter : UiPresenter<SearchLyricState> {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private val vm: SearchLyricViewModel by KoinJavaComponent.inject(SearchLyricViewModel::class.java)
    private var keywords by mutableStateOf("")
    private var lastSearchMediaId by mutableStateOf("")
    private var selectedId by mutableIntStateOf(-1)
    var mediaId by mutableStateOf("")
        private set

    @Composable
    override fun presentState(): SearchLyricState {
        return SearchLyricState(
            mediaId = mediaId,
            selectedId = selectedId,
            onAction = this::onAction
        )
    }

    override fun onAction(action: UiAction) {
        when (action) {
            is SearchLyricAction.SearchFor -> {
                if (lastSearchMediaId == mediaId && keywords == action.keywords) return
                vm.searchFor(song = action.keywords)

                keywords = action.keywords
                lastSearchMediaId = mediaId
            }

            is SearchLyricAction.SaveFor -> {
                vm.saveLyricInto(lyricId = selectedId, mediaId = mediaId) {
                    launch { NavigationWrapper.navigator?.pop() }
                }
            }

            is SearchLyricAction.UpdateMediaId -> mediaId = action.mediaId
            is SearchLyricAction.UpdateSelectedId -> selectedId = action.selectedId
        }
    }
}