package com.example.magicfrontiers.di

import com.example.magicfrontiers.viewmodel.GameViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
viewModel { GameViewModel(get()) }
}