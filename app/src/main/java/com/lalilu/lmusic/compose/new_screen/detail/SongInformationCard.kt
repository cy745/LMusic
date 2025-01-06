package com.lalilu.lmusic.compose.new_screen.detail

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.lmedia.entity.LSong
import java.text.DateFormat
import java.text.SimpleDateFormat

@Composable
fun SongInformationCard(
    modifier: Modifier = Modifier,
    song: LSong
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            song.metadata.genre.takeIf(String::isNotBlank)?.let {
                ColumnItem(
                    title = "流派",
                    content = it,
                )
            }

            song.fileInfo.mimeType.let { mimeType ->
                ColumnItem(
                    title = "文件类型",
                    content = mimeType,
                )
            }

            ColumnItem(
                title = "文件大小",
                content = remember(song) {
                    ConvertUtils.byte2FitMemorySize(song.fileInfo.size)
                },
            )

            ColumnItem(
                title = "平均码率",
                content = remember(song) { "%.1f kbps".format(song.fileInfo.bitrate / 1000f) },
            )

            song.metadata.dateAdded.let { date ->
                ColumnItem(
                    title = "添加日期",
                    content = remember(date) {
                        val time = date * 1000L
                        val dateS = SimpleDateFormat.getDateInstance(DateFormat.LONG).format(time)
                        val timeS = SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM).format(time)

                        "$dateS $timeS"
                    },
                )
            }

            if (song.metadata.disc.isNotBlank() || song.metadata.track.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    song.metadata.disc.takeIf(String::isNotBlank)?.let { disc ->
                        ColumnItem(
                            modifier = Modifier.weight(1f),
                            title = "光盘号",
                            content = disc,
                        )
                    }
                    song.metadata.track.takeIf(String::isNotBlank)?.let { track ->
                        ColumnItem(
                            modifier = Modifier.weight(1f),
                            title = "音轨号",
                            content = track,
                        )
                    }
                }
            }

            song.fileInfo.pathStr?.takeIf { it.isNotBlank() }?.let { path ->
                ColumnItem(
                    title = "文件位置",
                    content = path,
                    verticalAlignment = Alignment.Top,
                    showBorder = false
                )
            }
        }
    }
}

@Composable
fun ColumnItem(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    showBorder: Boolean = true
) {
    val clipboard = LocalClipboardManager.current
    val contentColor = MaterialTheme.colors.onBackground

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                if (showBorder) {
                    drawLine(
                        color = contentColor.copy(0.15f),
                        start = Offset(16.dp.toPx(), this.size.height),
                        end = Offset(this.size.width - 16.dp.toPx(), this.size.height),
                        cap = StrokeCap.Round
                    )
                }
            }
            .combinedClickable(
                onLongClick = {
                    clipboard.setText(buildAnnotatedString { append(content) })
                    ToastUtils.showShort("复制成功")
                },
                onClick = { ToastUtils.showShort("长按复制元素内容") }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle2,
            fontWeight = FontWeight.Bold,
            lineHeight = MaterialTheme.typography.subtitle2.fontSize
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .alpha(0.9f),
            text = content,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.caption,
        )
    }
}