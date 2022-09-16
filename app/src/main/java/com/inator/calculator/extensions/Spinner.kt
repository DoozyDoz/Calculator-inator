package com.inator.calculator.extensions

import android.content.res.Configuration
import android.widget.Spinner

val Spinner.isLandscape get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE