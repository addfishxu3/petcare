package com.example.petcare.data

import java.io.Serializable

data class HospitalInfo(
    val features: List<Feature>
):Serializable

data class Feature(
    val VeterinaryId: Int,
    val UserId: String,
    val City: String,
    val LicenceNumber: String,
    val LicenceType: String,
    val LicenceDate: String,
    val Status: String,
    val Name: String,
    val OwnName: String,
    val Tel: String,
    val Address: String,
    val CreateTime: String,
    val UpdateTime: String,
    val DataSource: String,
    val EmergencyInfo: String,
    val PriorityOrder: Int,
    val Enabled: Int,
    val BusinessHour: String,
    val WebSite: String,
    val EmergencyDepartment: String,
    val Views: Int,
    val WebTitle: String,
    val IsEmergencyDepartment: Boolean,
    val Summary: String,
    val Keyword: String
):Serializable