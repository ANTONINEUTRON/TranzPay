package com.neutron.tranzbit.data.model

data class User(
    val id: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null,
    var balance: Double? = null,
    val email: String? = null,
    val location: List<Double>? = null,
    val password: String? = null
)
