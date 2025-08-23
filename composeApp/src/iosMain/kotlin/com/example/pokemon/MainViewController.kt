package com.example.pokemon

import androidx.compose.ui.window.ComposeUIViewController
import com.example.pokemon.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App() }