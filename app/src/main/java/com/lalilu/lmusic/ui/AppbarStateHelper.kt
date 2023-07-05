package com.lalilu.lmusic.ui

open class AppbarStateHelper(appbar: CoverAppbar) : AppbarOffsetHelper(appbar) {

    sealed class State {
        object COLLAPSED : State()
        object EMPTY : State()
        object NORMAL : State()
        object EMPTY2 : State()
        object EXPENDED : State()
    }

    var dragThreshold: Int = 200
    var lastState: State = State.NORMAL

    var state: State = State.NORMAL
        private set(value) {
            if (field == value) return
            lastState = field
//            println("[State]: ${lastState::class.simpleName} -> ${value::class.simpleName}")
            field = value
        }

    override fun setPosition(value: Number) {
        super.setPosition(value)
        updateStateIfNeeded()
    }

    override fun snapBy(position: Int) {
        when (state) {
            State.COLLAPSED, State.EMPTY, State.NORMAL -> {
                animateTo(calcSnapToOffset(position, minPosition, 0))
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
            val percent = 1f - position.toFloat() / dragThreshold.toFloat() * 0.5f
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
            in (minPosition + dragThreshold) until -dragThreshold -> State.EMPTY
            in -dragThreshold until dragThreshold -> State.NORMAL
            in dragThreshold until (maxPosition - dragThreshold) -> State.EMPTY2
            else -> State.EXPENDED
        }
    }

    private fun getSnapPositionByState(state: State, lastState: State): Int {
        return when (state) {
            State.COLLAPSED -> minPosition
            State.NORMAL -> 0
            State.EXPENDED -> maxPosition

            State.EMPTY -> when (lastState) {
                State.COLLAPSED -> 0
                State.NORMAL -> minPosition
                else -> calcSnapToOffset(position, 0, minPosition)
            }

            State.EMPTY2 -> when (lastState) {
                State.NORMAL -> maxPosition
                State.EXPENDED -> 0
                else -> calcSnapToOffset(position, 0, maxPosition)
            }
        }
    }
}