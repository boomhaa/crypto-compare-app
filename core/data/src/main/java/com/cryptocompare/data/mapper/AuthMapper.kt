package com.cryptocompare.data.mapper

import com.cryptocompare.model.AuthUser
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toAuthUser(): AuthUser =
    AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
    )
