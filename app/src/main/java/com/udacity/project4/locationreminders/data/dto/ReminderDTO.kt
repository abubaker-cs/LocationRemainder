package com.udacity.project4.locationreminders.data.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Immutable model class for a Reminder. In order to compile with Room
 *
 * @param title         title of the reminder
 * @param description   description of the reminder
 * @param location      location name of the reminder
 * @param latitude      latitude of the reminder location
 * @param longitude     longitude of the reminder location
 * @param id          id of the reminder
 */

@Entity(tableName = "reminders")
data class ReminderDTO(

    // Title
    @ColumnInfo(name = "title") var title: String?,

    // Description
    @ColumnInfo(name = "description") var description: String?,

    // Location
    @ColumnInfo(name = "location") var location: String?,

    // These columns will be used for storing Geo-location information
    @ColumnInfo(name = "latitude") var latitude: Double?,
    @ColumnInfo(name = "longitude") var longitude: Double?,

    // ID
    @PrimaryKey @ColumnInfo(name = "entry_id") val id: String = UUID.randomUUID().toString()
)
