package com.inator.calculator.conversion.presentation


sealed class ConversionEvent {
    object PrepareToConvert : ConversionEvent()
    data class SetMeasure(val measure: String) : ConversionEvent()
    data class UnitOneSelected(val unit: String) : ConversionEvent()
    data class InputOneInput(val input: String) : ConversionEvent()
    data class UnitTwoSelected(val unit: String) : ConversionEvent()
    data class InputTwoInput(val input: String) : ConversionEvent()
}