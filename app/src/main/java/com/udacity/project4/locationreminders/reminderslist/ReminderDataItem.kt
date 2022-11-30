package com.udacity.project4.locationreminders.reminderslist

import java.io.Serializable
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
data class ReminderDataItem(

    // Title
    var title: String?,

    // Description
    var description: String?,

    // Location
    var location: String?,

    // Reference for storing Geo-location
    var latitude: Double?,
    var longitude: Double?,

    // ID
    val id: String = UUID.randomUUID().toString()

) : Serializable
