package com.lalilu.lmusic.screen.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.KeyboardUtils
import com.lalilu.databinding.FragmentInputerBinding
import com.lalilu.lmusic.utils.extension.getActivity

/**
 *
 * @param defaultValue  输入框中默认的值
 * @param onCommit      输入框的提交事件回调
 * @param onFocusChange 输入框的焦点变更事件回调，入参为当前的焦点情况，返回值为是否触发onCommit提交事件
 */
@Composable
fun RowScope.InputBar(
    hint: String = "",
    defaultValue: String = "",
    value: MutableState<String> = remember { mutableStateOf(defaultValue) },
    onFocusChange: (Boolean) -> Boolean = { false },
    onCommit: (String) -> Unit = {}
) {
    AndroidViewBinding(
        modifier = Modifier.weight(1f),
        factory = { inflater, parent, attachToParent ->
            FragmentInputerBinding.inflate(inflater, parent, attachToParent).apply {
                val activity = parent.context.getActivity()!!
                searchForLyricKeyword.setText(value.value)

                hint.takeIf { it.isNotEmpty() }?.let {
                    searchForLyricKeyword.hint = it
                }

                searchForLyricKeyword.addTextChangedListener {
                    value.value = it.toString()
                }

                searchForLyricKeyword.setOnEditorActionListener { textView, _, _ ->
                    value.value = textView.text.toString()
                    onCommit(value.value)
                    textView.clearFocus()
                    KeyboardUtils.hideSoftInput(textView)
                    return@setOnEditorActionListener true
                }

                KeyboardUtils.registerSoftInputChangedListener(activity) {
                    if (searchForLyricKeyword.isFocused && it > 0) {
                        return@registerSoftInputChangedListener
                    }

                    searchForLyricKeyword.clearFocus()
                    if (searchForLyricKeyword.isFocused && onFocusChange(searchForLyricKeyword.isFocused)) {
                        searchForLyricKeyword.onEditorAction(0)
                    }
                }
            }
        }
    ) {
        if (searchForLyricKeyword.text.toString() != value.value) {
            searchForLyricKeyword.setText(value.value)
        }
    }
}

