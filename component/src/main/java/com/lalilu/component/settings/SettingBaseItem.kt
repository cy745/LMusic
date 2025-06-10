package com.lalilu.component.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.extension.enableFor

@Composable
fun SettingBaseItem(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    textColor: Color = MaterialTheme.colors.onBackground,
    enableContentClickable: Boolean = true,
    onContentStartClick: () -> Unit = {},
    contentEnd: @Composable (MutableInteractionSource) -> Unit = {},
    contentBottom: @Composable (MutableInteractionSource) -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .enableFor(enable = { enableContentClickable }) {
                    clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onContentStartClick
                    )
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                content = {
                    Text(
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            spacing = MarqueeSpacing(30.dp)
                        ),
                        text = title,
                        color = textColor,
                        fontSize = 14.sp
                    )
                    if (subTitle != null) {
                        Text(
                            modifier = Modifier.basicMarquee(
                                iterations = Int.MAX_VALUE,
                                spacing = MarqueeSpacing(30.dp)
                            ),
                            text = subTitle,
                            fontSize = 12.sp,
                            color = textColor.copy(0.5f)
                        )
                    }
                }
            )
            contentEnd(interactionSource)
        }

        contentBottom(interactionSource)
    }
}