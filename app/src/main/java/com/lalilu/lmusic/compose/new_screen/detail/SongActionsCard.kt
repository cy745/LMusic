package com.lalilu.lmusic.compose.new_screen.detail

import android.content.ComponentName
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lalilu.component.IconTextButton
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.new_screen.SearchLyricScreen
import com.lalilu.lmusic.utils.extension.checkActivityIsExist

@Composable
fun SongActionsCard(
    modifier: Modifier = Modifier,
    song: LSong,
) {
    val context = LocalContext.current
    val intent = remember(song) {
        Intent().apply {
            component = ComponentName(
                "com.xjcheng.musictageditor",
                "com.xjcheng.musictageditor.SongDetailActivity"
            )
            action = "android.intent.action.VIEW"
            data = song.uri
        }
    }


    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (context.checkActivityIsExist(intent)) {
                IconTextButton(
                    text = "音乐标签编辑",
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF3EA22C),
                    onClick = {
                        if (context.checkActivityIsExist(intent)) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(
                                context,
                                "未安装[音乐标签]",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
            IconTextButton(
                text = "搜索LrcShare",
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF3EA22C),
                onClick = {
                    AppRouter.intent(
                        NavIntent.Push(
                            SearchLyricScreen(
                                mediaId = song.id,
                                keywords = song.name
                            )
                        )
                    )
                }
            )
        }
    }
}