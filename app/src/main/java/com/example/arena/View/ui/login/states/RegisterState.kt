package com.example.arena.View.ui.login.states

sealed interface RegisterState {

    object idle : RegisterState
    object loading : RegisterState
    object success : RegisterState
    data class Error(val message: String) : RegisterState
}