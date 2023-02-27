package com.lalilu.lmusic.compose.new_screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.base.SearchInputBar
import com.lalilu.lmusic.viewmodel.SearchViewModel
import com.ramcosta.composedestinations.annotation.Destination
import org.koin.androidx.compose.get

@SearchNavGraph(start = true)
@Destination
@Composable
fun SearchScreen(
    searchVM: SearchViewModel = get()
) {
    val keyword = remember { mutableStateOf(searchVM.keywordStr.value) }
    val showSearchBar = remember { mutableStateOf(true) }

    SmartBar.RegisterExtraBarContent(showSearchBar) {
        SearchInputBar(
            value = keyword,
            onSubmit = {
                searchVM.searchFor(keyword.value)
            }
        )
    }
}
