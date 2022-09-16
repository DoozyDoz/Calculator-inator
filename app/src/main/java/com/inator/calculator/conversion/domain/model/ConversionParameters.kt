package com.inator.calculator.conversion.domain.model

data class ConversionParameters(
    val measure: String,
    val unitOne: String,
    val inputOne: String,
    val unitTwo: String,
    val inputTwo: String
)