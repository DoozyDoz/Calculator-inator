package com.inator.calculator.convert.presentation

data class ConverterFragmentViewState(
    val chipName: String = "Length",
    val unit1: Int = 0,
    val input1: String = "0.0",
    val unit2: Int = 0,
    val input2: String = "0.0"
)