package com.joy.common.widget.popup

data class QuickMenuBean(
    val iconText: String,
    val title: String,
    val badgeCount: Int = 0,
    val onClick: () -> Unit,
)
