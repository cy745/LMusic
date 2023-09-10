package com.lalilu.lmusic.ui


typealias OnStateChangeListener = (
    newState: AppbarStateHelper.State,
    oldState: AppbarStateHelper.State,
    fromUser: Boolean,
) -> Unit

open class AppbarStateHelper(appbar: CoverAppbar) : AppbarOffsetHelper(appbar) {

    sealed class State(val value: Int) {
        data object COLLAPSED : State(0)
        data object EMPTY : State(1)
        data object NORMAL : State(2)
        data object EMPTY2 : State(3)
        data object EXPENDED : State(4)

        companion object {
            fun from(value: Int): State = when (value) {
                0 -> COLLAPSED
                1 -> EMPTY
                2 -> NORMAL
                3 -> EMPTY2
                4 -> EXPENDED
                else -> NORMAL
            }
        }
    }

    open var dragThreshold: Int = 120
    private val stateChangeListeners = hashSetOf<OnStateChangeListener>()

    fun addOnStateChangeListener(listener: OnStateChangeListener) {
        stateChangeListeners.add(listener)
    }

    var lastState: State = State.NORMAL
        private set

    var state: State = State.NORMAL
        private set

    fun restoreState(state: State, lastState: State) {
        this.state = state
        this.lastState = lastState
    }

    override fun onViewLayout() {
        if (appbar.bottom != position && position == INVALID_POSITION) {
            if (!isValueValidNow()) return
            updatePosition(getSnapPositionByState(state, lastState))
        }
        super.onViewLayout()
    }

    open fun animateToState(newState: State) {
        val targetOffset = getSnapPositionByState(newState, state)
        animateTo(targetOffset)
    }

    override fun updatePosition(value: Number) {
        super.updatePosition(value)
        updateStateIfNeeded()
    }

    override fun snapBy(position: Int, fromUser: Boolean) {
        when (state) {
            State.COLLAPSED, State.EMPTY, State.NORMAL -> {
                animateTo(calcSnapToOffset(position, minPosition, middlePosition))
            }

            else -> snapIfNeeded(fromUser = fromUser)
        }
    }

    override fun snapIfNeeded(fromUser: Boolean) {
        val targetPosition = getSnapPositionByState(state, lastState)
        actionFromUser = fromUser
        animateTo(targetPosition)
    }


    override fun scrollBy(dy: Int): Int {
        return super.scrollBy(checkDampOffset(dy))
    }

    open fun checkDampOffset(dy: Int): Int {
        var result = dy
        if (dy < 0) {
            val percent =
                1f - (position.toFloat() - middlePosition.toFloat()) / dragThreshold.toFloat() * 0.5f
            if (percent in 0F..1F) result = (dy * percent).toInt()
        }
        return result
    }

    private fun updateStateIfNeeded() {
        val newState = getStateByPosition(position)

        if (newState == state) return
        lastState = state
        state = newState

        for (listener in stateChangeListeners) {
            listener.invoke(state, lastState, actionFromUser)
        }
    }

    private fun getStateByPosition(value: Int): State {
        return when (value) {
            in minPosition until (minPosition + dragThreshold) -> State.COLLAPSED
            in (minPosition + dragThreshold) until (middlePosition - dragThreshold) -> State.EMPTY
            in (middlePosition - dragThreshold) until (middlePosition + dragThreshold) -> State.NORMAL
            in (middlePosition + dragThreshold) until (maxPosition - dragThreshold) -> State.EMPTY2
            else -> State.EXPENDED
        }
    }

    private fun getSnapPositionByState(state: State, lastState: State): Int {
        return when (state) {
            State.COLLAPSED -> minPosition
            State.NORMAL -> middlePosition
            State.EXPENDED -> maxPosition

            State.EMPTY -> when (lastState) {
                State.COLLAPSED -> middlePosition
                State.NORMAL -> minPosition
                else -> calcSnapToOffset(position, middlePosition, minPosition)
            }

            State.EMPTY2 -> when (lastState) {
                State.NORMAL -> maxPosition
                State.EXPENDED -> middlePosition
                else -> calcSnapToOffset(position, middlePosition, maxPosition)
            }
        }
    }
}