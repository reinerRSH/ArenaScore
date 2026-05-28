package com.example.arena.ui.auth.register

sealed interface RegisterState {

    object idle : RegisterState
    object loading : RegisterState
    object success : RegisterState
    data class Error(val message: String) : RegisterState
}