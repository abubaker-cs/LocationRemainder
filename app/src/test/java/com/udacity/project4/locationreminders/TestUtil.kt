package com.udacity.project4.locationreminders

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {

    // data is initially set to null
    var data: T? = null

    // latch is used to block the thread until the data is set
    val latch = CountDownLatch(1)

    // This will be used to observe the live data
    val observer = object : Observer<T> {

        // on changed is called when the data is set
        override fun onChanged(o: T?) {

            // set the data
            data = o

            // unblock the thread
            latch.countDown()

            // remove the observer
            this@getOrAwaitValue.removeObserver(this)

        }

    }

    // Observe the LiveData forever
    this.observeForever(observer)

    try {

        // afterObserve is called to perform any action before blocking the thread
        afterObserve.invoke()

        // block the thread until the data is set or the timeout is reached
        if (!latch.await(time, timeUnit)) {

            // if the timeout is reached throw an exception error: "LiveData value was never set."
            throw TimeoutException("LiveData value was never set.")

        }

    } finally {

        // removeObserver is called to remove the observer
        this.removeObserver(observer)

    }

    // return the data
    @Suppress("UNCHECKED_CAST")
    return data as T
}
