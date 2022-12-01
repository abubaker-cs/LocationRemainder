package com.udacity.project4.base

import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView

/**
 * View Holder for the Recycler View to bind the data item to the UI
 */
class DataBindingViewHolder<T>(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: T) {


        /**
         * Set the variable id of the data item to the binding
         *
         * EXPLANATION
         * ===========
         * 1. At times, the specific binding class isn't known. For example, a RecyclerView.Adapter
         * operating against arbitrary layouts doesn't know the specific binding class. It still must
         * assign the binding value during the call to the onBindViewHolder() method.
         *
         * 2. The Data Binding Library generates a class named BR in the module package which
         * contains the IDs of the resources used for data binding. In the following code, the
         * library automatically generates the BR.item variable.
         *
         * Reference: https://developer.android.com/topic/libraries/data-binding/generated-binding
         */
        binding.setVariable(BR.item, item)

        // This is necessary to ensure the data binding is executed immediately
        binding.executePendingBindings()

    }

}
