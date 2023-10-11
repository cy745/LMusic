package com.lalilu.lplayer.extensions

import com.lalilu.lplayer.LPlayer

sealed class PlayerAction : Action {
    override fun action(): Boolean = LPlayer.controller.doAction(this)

    data object Play : PlayerAction()
    data object Pause : PlayerAction()
    data object SkipToNext : PlayerAction()
    data object SkipToPrevious : PlayerAction()
    data class PlayById(val mediaId: String) : PlayerAction()
    data class SeekTo(val positionMs: Long) : PlayerAction()

    sealed class CustomAction(val name: String) : PlayerAction()
    data object PlayOrPause : CustomAction(PlayOrPause::class.java.name)
    data object ReloadAndPlay : CustomAction(ReloadAndPlay::class.java.name)

    companion object {
        fun of(name: String): CustomAction? {
            return when (name) {
                PlayOrPause::class.java.name -> return PlayOrPause
                ReloadAndPlay::class.java.name -> return ReloadAndPlay
                else -> null
            }
        }
    }
}

