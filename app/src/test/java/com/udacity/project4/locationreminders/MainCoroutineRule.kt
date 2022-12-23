@file:Suppress("DEPRECATION")

package com.udacity.project4.locationreminders

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class MainCoroutineRule(private val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) :
    TestWatcher(),
    TestCoroutineScope by createTestCoroutineScope(TestCoroutineDispatcher() + TestCoroutineExceptionHandler() + dispatcher) {

    // starting() is called before each test
    override fun starting(description: Description) {

        // Pass the description of the test, once it is about to start
        super.starting(description)

        // Set the Main dispatcher to a TestCoroutineDispatcher
        Dispatchers.setMain(dispatcher)
    }

    // finished() is called after each test
    override fun finished(description: Description) {

        // Pass the description of the test, once it will finish
        super.finished(description)


        /**
         * cleanupTestCoroutines() is called after the test completes:
         *
         * 1. It checks that there were no uncaught exceptions caught by its CoroutineExceptionHandler.
         *    If there were any, then the first one is thrown, whereas the rest are suppressed by it.
         *
         * 2. It runs the tasks pending in the scheduler at the current time. If there are any
         *    uncompleted tasks afterwards, it fails with UncompletedCoroutinesError.
         *
         * 3. It checks whether some new child Jobs were created but not completed since this
         *    TestCoroutineScope was created. If so, it fails with UncompletedCoroutinesError.
         *
         *    Reference: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/kotlinx.coroutines.test/-test-coroutine-scope/cleanup-test-coroutines.html
         */
        cleanupTestCoroutines()

        // Reset main dispatcher to the original Main dispatcher
        Dispatchers.resetMain()

    }

}
