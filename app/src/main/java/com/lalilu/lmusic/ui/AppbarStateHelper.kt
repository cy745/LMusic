package com.lalilu.lmusic.ui


typealias OnStateChangeListener = (
    newState: AppbarStateHelper.State,
    oldState: AppbarStateHelper.State,
    fromUser: Boolean,
) -> Unit

open class AppbarStateHelper(appbar: CoverAppbar) : AppbarOffsetHelper(appbar) {

    sealed class State {
        object COLLAPSED : State()
        object EMPTY : State()
        object NORMAL : State()
        object EMPTY2 : State()
        object EXPENDED : State()
    }

    open var dragThreshold: Int = 120
    private val stateChangeListeners = hashSetOf<OnStateChangeListener>()

    fun addOnStateChangeListener(listener: OnStateChangeListener) {
        stateChangeListeners.add(listener)
    }

    var lastState: State = State.NORMAL
        private set

    var state: State = State.NORMAL
        private set(value) {
            if (field == value) return
            lastState = field
            field = value
            for (listener in stateChangeListeners) {
                listener.invoke(value, lastState, actionFromUser)
            }
//            println("[State]: ${lastState::class.simpleName} -> ${value::class.simpleName}")
        }

    override fun setPosition(value: Number) {
        super.setPosition(value)
        updateStateIfNeeded()
    }

    override fun snapBy(position: Int) {
        when (state) {
            State.COLLAPSED, State.EMPTY, State.NORMAL -> {
                animateTo(calcSnapToOffset(position, minPosition, middlePosition))
            }

            else -> snapIfNeeded()
        }
    }

    override fun snapIfNeeded() {
        val targetPosition = getSnapPositionByState(state, lastState)
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
        state = getStateByPosition(position)
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