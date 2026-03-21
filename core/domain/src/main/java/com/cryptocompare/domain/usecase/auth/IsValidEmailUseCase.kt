package com.cryptocompare.domain.usecase.auth

import com.cryptocompare.helpers.util.Constants
import javax.inject.Inject

class IsValidEmailUseCase
    @Inject
    constructor() {
        operator fun invoke(email: String): Boolean =
            email.isNotBlank() && Constants.UseCaseConstants.EMAIL_REGEX.matches(email)
    }
