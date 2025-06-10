package com.lalilu.component.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.lalilu.RemixIcon
import com.lalilu.component.lumo.components.Accordion
import com.lalilu.component.lumo.components.rememberAccordionState
import com.lalilu.remixicon.Arrows
import com.lalilu.remixicon.arrows.arrowDownSLine

@Composable
fun SettingStateAccordion(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val bgColor = MaterialTheme.colors.onBackground.copy(0.05f)
    val accordionState = rememberAccordionState()

    Accordion(
        modifier = modifier.drawBehind {
            drawRect(
                color = bgColor,
                alpha = accordionState.animationProgress
            )
        },
        state = accordionState,
        headerContent = {
            SettingBaseItem(
                enableContentClickable = false,
                title = title,
                subTitle = subTitle,
                contentEnd = {
                    Icon(
                        modifier = Modifier.rotate(accordionState.animationProgress * 180),
                        imageVector = RemixIcon.Arrows.arrowDownSLine,
                        tint = MaterialTheme.colors.onBackground,
                        contentDescription = null
                    )
                },
                contentBottom = {
                    HorizontalDivider(
                        modifier = Modifier.graphicsLayer {
                            alpha = accordionState.animationProgress
                        },
                        color = MaterialTheme.colors.onBackground.copy(0.2f)
                    )
                }
            )
        },
        bodyContent = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content
            )
        }
    )
}