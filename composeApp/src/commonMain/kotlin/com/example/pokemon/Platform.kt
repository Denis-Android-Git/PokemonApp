package com.example.pokemon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform