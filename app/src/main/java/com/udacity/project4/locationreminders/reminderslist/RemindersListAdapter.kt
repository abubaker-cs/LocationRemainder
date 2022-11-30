package com.udacity.project4.locationreminders.reminderslist

import com.udacity.project4.R
import com.udacity.project4.base.BaseRecyclerViewAdapter


//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {

    // Get reference to the @layout/reminder_list_item.xml layout file, that will be used for creating
    // individual list item.
    override fun getLayoutRes(viewType: Int) = R.layout.reminder_list_item

}
