package com.example.petcare.data

data class UserData(
    val user: String? = null,
    val pwd: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val petList: List<PetData>? = null
)

data class PetData(
    val name: String? = null,
    val type: String? = null,
    val gender: String? = null,
    val age: String? = null
)