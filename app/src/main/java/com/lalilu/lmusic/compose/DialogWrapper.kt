package com.lalilu.lmusic.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lalilu.component.extension.dayNightTextColor
import com.melody.dialog.any_pop.AnyPopDialog
import com.melody.dialog.any_pop.AnyPopDialogProperties
import com.melody.dialog.any_pop.DirectionState


interface DialogContext {
    fun dismiss()
    fun isVisible(): Boolean
}

private val DEFAULT_DIALOG_PROPERTIES = AnyPopDialogProperties(
    direction = DirectionState.BOTTOM
)

sealed class DialogItem {
    data class Static(
        val title: String,
        val message: String,
        val properties: AnyPopDialogProperties = DEFAULT_DIALOG_PROPERTIES,
        val onConfirm: () -> Unit = {},
        val onCancel: () -> Unit = {},
        val onDismiss: () -> Unit = {},
    ) : DialogItem()

    data class Dynamic(
        val properties: AnyPopDialogProperties = DEFAULT_DIALOG_PROPERTIES,
        val content: @Composable DialogContext.() -> Unit,
    ) : DialogItem()
}

object DialogWrapper : DialogContext {
    private var dialogItem by mutableStateOf<DialogItem?>(null)
    private var dismissFunc by mutableStateOf<(() -> Unit)?>(null)

    override fun isVisible(): Boolean = dialogItem != null
    override fun dismiss(): Unit = run { dismissFunc?.invoke() }

    fun push(item: DialogItem) {
        dialogItem = item
    }

    @Composable
    fun Content() {
        if (dialogItem != null) {
            val isActiveClose by remember {
                mutableStateOf(false)
                    .also { dismissFunc = { it.value = true } }
            }

            val properties = remember(dialogItem) {
                dialogItem?.let {
                    when (it) {
                        is DialogItem.Dynamic -> it.properties
                        is DialogItem.Static -> it.properties
                    }
                } ?: DEFAULT_DIALOG_PROPERTIES
            }

            AnyPopDialog(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp)
                    .background(color = MaterialTheme.colors.background),
                isActiveClose = isActiveClose,
                properties = properties,
                onDismiss = {
                    (dialogItem as? DialogItem.Static)?.onDismiss?.invoke()
                    dialogItem = null
                },
                content = {
                    dialogItem?.let {
                        when (it) {
                            is DialogItem.Static -> {
                                StaticDialogCard(
                                    title = it.title,
                                    message = it.message,
                                    onConfirm = {
                                        dismiss()
                                        it.onConfirm()
                                    },
                                    onCancel = {
                                        dismiss()
                                        it.onCancel()
                                    }
                                )
                            }

                            is DialogItem.Dynamic -> it.content.invoke(this@DialogWrapper)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun StaticDialogCard(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = modifier
                .weight(1f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                color = dayNightTextColor()
            )
            Text(
                text = message,
                style = MaterialTheme.typography.subtitle2,
                color = dayNightTextColor(0.3f)
            )
        }

        val cancelColor = remember { Color(0xFFFF7575) }
        val confirmColor = remember { Color(0xFF258302) }

        TextButton(
            shape = RectangleShape,
            modifier = Modifier
                .heightIn(min = 56.dp)
                .widthIn(min = 84.dp),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = cancelColor.copy(alpha = 0.15f),
                contentColor = cancelColor
            ),
            onClick = onCancel
        ) {
            Text(text = "取消")
        }
        TextButton(
            shape = RectangleShape,
            modifier = Modifier
                .heightIn(min = 56.dp)
                .widthIn(min = 84.dp),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = confirmColor.copy(alpha = 0.15f),
                contentColor = confirmColor
            ),
            onClick = onConfirm
        ) {
            Text(text = "确认")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StaticCardDialogPreview() {
    StaticDialogCard(title = "是否需要删除文件{}", message = "确认删除吗？")
}