package com.lalilu.lmusic.compose.new_screen.detail

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.ConvertUtils
import com.lalilu.lmedia.entity.LSong
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            song.fileInfo.mimeType.let { mimeType ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "文件类型",
                        style = MaterialTheme.typography.subtitle2
                    )
                    Text(
                        text = mimeType,
                        style = MaterialTheme.typography.caption
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "文件大小",
                    style = MaterialTheme.typography.subtitle2
                )
                Text(
                    text = ConvertUtils.byte2FitMemorySize(song.fileInfo.size),
                    style = MaterialTheme.typography.caption
                )
            }

            song.metadata.dateAdded.let { date ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "添加日期",
                        style = MaterialTheme.typography.subtitle2
                    )
                    Text(
                        text = SimpleDateFormat.getDateInstance().format(date * 1000L),
                        style = MaterialTheme.typography.caption
                    )
                }
            }

            if (song.metadata.disc.isNotBlank() || song.metadata.track.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    song.metadata.disc.takeIf { it.isNotBlank() }?.let { disc ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "光盘号",
                                style = MaterialTheme.typography.subtitle2
                            )
                            Text(
                                text = disc,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                    song.metadata.track.takeIf { it.isNotBlank() }?.let { track ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "音轨号",
                                style = MaterialTheme.typography.subtitle2
                            )
                            Text(
                                text = track,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                }
            }

            song.fileInfo.pathStr?.takeIf { it.isNotBlank() }?.let { path ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "文件位置",
                        style = MaterialTheme.typography.subtitle2
                    )
                    Text(
                        text = path,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}