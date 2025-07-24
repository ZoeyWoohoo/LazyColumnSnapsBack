package com.example.lazycolumnsnapsback

data class State(
    val items: List<Int> = emptyList()
)

sealed class Intent {
    data object LoadData : Intent()
}

sealed class Effect {
    data class ScrollListToBottom(val animate: Boolean, val index: Int) : Effect()
}