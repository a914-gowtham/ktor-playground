package com.example.db.model

import io.ktor.auth.*

data class User(val email: String,val hashPwd: String,
                val name: String) : Principal
