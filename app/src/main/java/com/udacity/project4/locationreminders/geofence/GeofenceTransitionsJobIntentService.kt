package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        //  This will be used to tart the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        //TO DO: handle the geofencing transition events and send a notification to the user when he enters the geofence area
        //TO DO call @sendNotification

        // Get the geofencing event from the intent
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent != null) {

            // Check if there are any errors
            if (geofencingEvent.hasError()) {
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                geofencingEvent.triggeringGeofences?.let { sendNotification(it) }
            }

        }

    }

    //TO DO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {

        triggeringGeofences.forEach {

            // Get the request ID of the current geofence
            val requestId = it.requestId

            // Get the local repository instance
            val remindersLocalRepository: RemindersLocalRepository by inject()

            // Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {

                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)

                // Check if the result is a success
                if (result is Result.Success<ReminderDTO>) {

                    // Get the reminder data item
                    val reminderDTO = result.data

                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )

                }
            }
        }

    }

}
