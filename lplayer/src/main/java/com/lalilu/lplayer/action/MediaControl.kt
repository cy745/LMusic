package com.lalilu.lplayer.action

import com.lalilu.lplayer.MPlayer

/**
 * 常用的媒体操作的方法封装
 */
object MediaControl {

    /**
     * 将当前元素添加进播放列表的下一位置，并开始播放
     * 若当前播放歌曲就是目标歌曲，则暂停或播放
     */
    fun addAndPlay(mediaId: String) {
        // 将当前元素添加进播放列表的下一位置，若已存在则不移动
        PlayerAction.AddToNext(mediaId).action()

        if (MPlayer.currentMediaItem?.mediaId == mediaId) {
            PlayerAction.PlayOrPause.action()
        } else {
            PlayerAction.PlayById(mediaId = mediaId)
                .action()
        }
    }

    /**
     * 替换播放列表，并播放目标歌曲
     */
    fun playWithList(mediaIds: List<String>, mediaId: String) {
        PlayerAction.UpdateList(mediaIds, mediaId).action()
    }
}