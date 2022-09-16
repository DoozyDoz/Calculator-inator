package com.inator.calculator.common.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.inator.calculator.common.data.preferences.PreferencesConstants.KEY_CONVERTER_SPINNER_1
import com.inator.calculator.common.data.preferences.PreferencesConstants.KEY_CONVERTER_SPINNER_2
import com.inator.calculator.common.data.preferences.PreferencesConstants.KEY_CURRENCY_SPINNER_1
import com.inator.calculator.common.data.preferences.PreferencesConstants.KEY_CURRENCY_SPINNER_2
import com.inator.calculator.common.data.preferences.PreferencesConstants.KEY_MEASURE
import com.inator.calculator.model.Rate
import com.inator.calculator.repository.CONVERTER_SPINNER_1
import com.inator.calculator.repository.CONVERTER_SPINNER_2
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalculatorInatorPreferences @Inject constructor(
    @ApplicationContext context: Context
) : Preferences {
    companion object {
        const val PREFERENCES_NAME = "CALCULATOR_INATOR_PREFERENCES"
    }

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun putMeasure(measure: String) {
        edit { putString(KEY_MEASURE, measure) }
    }

    override fun putCurrencySpinnerOne(rate: Rate) {
        edit { putString(KEY_CURRENCY_SPINNER_1, rate.code) }
    }

    override fun putCurrencySpinnerTwo(rate: Rate) {
        edit { putString(KEY_CURRENCY_SPINNER_2, rate.code) }
    }

    override fun putConverterSpinnerOne(position: Int) {
        edit { putInt(KEY_CONVERTER_SPINNER_1, position) }
    }

    override fun putConverterSpinnerTwo(position: Int) {
        edit { putInt(KEY_CONVERTER_SPINNER_2, position) }
    }

    override fun getMeasure(): String {
        return preferences.getString(KEY_MEASURE, "Length") ?: "Length"
    }

    override fun getCurrencySpinnerOne(): String {
        return preferences.getString(KEY_CURRENCY_SPINNER_1, "AED") ?: "AED"
    }

    override fun getCurrencySpinnerTwo(): String {
        return preferences.getString(KEY_CURRENCY_SPINNER_2, "AFN") ?: "AFN"
    }

    override fun getConverterSpinnerOne(): Int {
        return preferences.getInt(KEY_CONVERTER_SPINNER_1, 0)
    }

    override fun getConverterSpinnerTwo(): Int {
        return preferences.getInt(KEY_CONVERTER_SPINNER_2, 1)
    }

    override fun deleteSpinners() {
        edit {
            remove(CONVERTER_SPINNER_1)
            remove(CONVERTER_SPINNER_2)
        }
    }


    private inline fun edit(block: SharedPreferences.Editor.() -> Unit) {
        with(preferences.edit()) {
            block()
            commit()
        }
    }
}