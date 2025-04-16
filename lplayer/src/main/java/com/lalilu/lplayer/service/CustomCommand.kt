package com.lalilu.lplayer.service

import android.os.Bundle
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import com.lalilu.lplayer.service.CustomCommand.SeekToNext
import com.lalilu.lplayer.service.CustomCommand.SeekToPrevious

internal enum class CustomCommand(val action: String) {
    SeekToNext(action = "com.lalilu.lplayer.service.command.next"),
    SeekToPrevious(action = "com.lalilu.lplayer.service.command.previous");

    fun toSessionCommand(): SessionCommand = SessionCommand(action, Bundle.EMPTY)
}

internal fun SessionCommand.toCustomCommendOrNull(): CustomCommand? {
    return when (customAction) {
        SeekToNext.action -> SeekToNext
        SeekToPrevious.action -> SeekToPrevious
        else -> null
    }
}

internal fun SessionCommands.Builder.registerCustomCommands(): SessionCommands.Builder = apply {
    addSessionCommands(CustomCommand.entries.map { it.toSessionCommand() })
}