package com.inator.calculator.conversion.presentation

import com.inator.calculator.common.presentation.Event

data class ConversionViewState(
    val currentMeasure: Int = 0,
    val chipName: String = "Length",
    val unit1: Int = 0,
    val input1: String = "0.0",
    val unit2: Int = 0,
    val input2: String = "0.0",
    val unitValues: Event<List<String>> = Event(emptyList()),
)