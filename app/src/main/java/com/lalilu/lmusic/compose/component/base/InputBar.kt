package com.lalilu.lmusic.compose.component.base

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

@Composable
fun InputBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    defaultValue: String = "",
    value: MutableState<String> = remember { mutableStateOf(defaultValue) },
    onSubmit: (String) -> Unit = {},
    onFocusChange: (Boolean) -> Boolean = { false },
) {
    AndroidViewBinding(
        modifier = modifier,
        factory = { inflater, parent, attachToParent ->
            FragmentInputerBinding.inflate(inflater, parent, attachToParent).apply {
                val activity = parent.context.getActivity()!!
                inputer.setText(value.value)

                hint.takeIf { it.isNotEmpty() }?.let {
                    inputer.hint = it
                }

                inputer.addTextChangedListener {
                    value.value = it.toString()
                }

                inputer.setOnEditorActionListener { textView, _, _ ->
                    value.value = textView.text.toString()
                    onSubmit(value.value)
                    textView.clearFocus()
                    KeyboardUtils.hideSoftInput(textView)
                    return@setOnEditorActionListener true
                }

                KeyboardUtils.registerSoftInputChangedListener(activity) {
                    if (inputer.isFocused && it > 0) {
                        return@registerSoftInputChangedListener
                    }

                    inputer.clearFocus()
                    if (inputer.isFocused && onFocusChange(inputer.isFocused)) {
                        inputer.onEditorAction(0)
                    }
                }
            }
        }
    ) {
        if (inputer.text.toString() != value.value) {
            inputer.setText(value.value)
        }
    }
}