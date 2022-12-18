package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.Constants.EXTRA_ReminderDataItem
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderDescriptionBinding


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Inflate the layout @layout/activity_reminder_description.xml
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )

        /**
         * reminderDataItem = Reference to the data variable in @layout/activity_reminder_description.xml
         * getSerializableExtra() = Retrieve extended data from the intent.
         */
        binding.reminderDataItem =
            intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem

        binding.executePendingBindings()

    }

    companion object {

        // receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {

            // create an intent and pass the reminder object to the ReminderDescriptionActivity
            val intent = Intent(context, ReminderDescriptionActivity::class.java)

            // put the reminder object in the intent
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)

            // return the intent
            return intent

        }

    }

}
