package com.lalilu.value_cat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ValueCat {
    private val valueMap = mutableStateMapOf<String, List<Float>>()

    fun catFor(key: String, value: Float) {
        val queue = valueMap.getOrPut(key) { emptyList() }.toMutableList()

        if (queue.size >= 19) queue.removeFirst()
        queue.add(value)

        valueMap[key] = queue
    }

    internal val content = @Composable {
        Box(
            modifier = Modifier.padding(5.dp)
        ) {
            Surface(
                elevation = 10.dp,
                shape = RoundedCornerShape(10.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    items(items = valueMap.entries.toList(), key = { it.key }) {
                        ValueRow(
                            key = it.key,
                            list = it.value
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ValueRow(key: String, list: List<Float>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val textMeasurer = rememberTextMeasurer()
        val textStyle = remember { TextStyle(fontSize = 12.sp) }

        Text(text = "$key: %2f".format(list.lastOrNull()))
        Canvas(
            modifier = Modifier
                .border(color = Color.Blue, width = 2.dp)
                .height(56.dp)
                .width(100.dp)
        ) {
            val average = list.average()

            val gap = size.width / list.size
            val middle = size.height / 2f

            val offsets = list.mapIndexed { index, fl ->
                Offset(x = index * gap, y = (middle + (fl - average)).toFloat())
            }
            val averageText = textMeasurer.measure(
                text = "%.1f".format(average),
                style = textStyle
            )

            drawLine(
                strokeWidth = 1f,
                color = Color.Blue,
                start = Offset(0f, middle),
                end = Offset(size.width, middle)
            )
            drawText(
                textLayoutResult = averageText,
                topLeft = Offset(0f, middle - averageText.size.height / 2f)
            )

            for (index in offsets.indices) {
                if (index == 0) continue

                drawLine(
                    color = Color.Red,
                    start = offsets[index - 1],
                    end = offsets[index],
                    strokeWidth = 2f
                )
            }
        }
    }
}