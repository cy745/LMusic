package com.lalilu.component.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import com.lalilu.RemixIcon
import com.lalilu.component.lumo.components.Accordion
import com.lalilu.component.lumo.components.AccordionGroupState
import com.lalilu.component.lumo.components.card.CardDefaults
import com.lalilu.component.lumo.components.card.OutlinedCard
import com.lalilu.remixicon.Arrows
import com.lalilu.remixicon.arrows.arrowDownSLine

@Composable
fun SettingBaseAccordion(
    modifier: Modifier = Modifier,
    index: Int = 0,
    groupState: AccordionGroupState,
    title: String,
    subTitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colors.surface)
    ) {
        val state = groupState.getState(index)

        Accordion(
            state = state,
            headerContent = {
                SettingBaseItem(
                    title = title,
                    subTitle = subTitle,
                    enableContentClickable = false,
                    contentEnd = {
                        Icon(
                            modifier = Modifier.rotate(state.animationProgress * 180),
                            tint = MaterialTheme.colors.onBackground,
                            imageVector = RemixIcon.Arrows.arrowDownSLine,
                            contentDescription = null
                        )
                    },
                    contentBottom = {
                        HorizontalDivider(
                            color = MaterialTheme.colors.onBackground.copy(0.2f)
                        )
                    }
                )
            },
            bodyContent = {
                Column(content = content)
            }
        )
    }
}