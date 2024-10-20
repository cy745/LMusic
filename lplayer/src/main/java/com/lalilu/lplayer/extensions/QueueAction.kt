package com.lalilu.lplayer.extensions

sealed class QueueAction : Action {
    override fun action() {
    }

    data object Clear : QueueAction()
    data object Shuffle : QueueAction()
    data class AddToNext(val id: String) : QueueAction()
    data class Remove(val id: String) : QueueAction()
    data class UpdatePlaying(val id: String?) : QueueAction()
    data class UpdateList(val ids: List<String>) : QueueAction()
}