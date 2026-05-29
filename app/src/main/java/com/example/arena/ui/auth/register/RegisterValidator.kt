package com.example.arena.ui.auth.register

import android.content.Context
import com.example.arena.R

object RegistrarValidator {

    fun validarFormulario(
        context: Context,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        return when {
            firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() ->
                context.getString(R.string.all_fields_required)

            password.length < 6 ->
                context.getString(R.string.password_too_short)

            password != confirmPassword ->
                context.getString(R.string.passwords_do_not_match)

            else -> null
        }
    }
}