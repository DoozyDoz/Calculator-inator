package com.inator.calculator.common.data.di

import com.inator.calculator.common.data.preferences.CalculatorInatorPreferences
import com.inator.calculator.common.data.preferences.Preferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

  @Binds
  abstract fun providePreferences(preferences: CalculatorInatorPreferences): Preferences
}
