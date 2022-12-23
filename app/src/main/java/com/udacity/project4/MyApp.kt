package com.udacity.project4

import android.app.Application
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp : Application() {

    override fun onCreate() {

        super.onCreate()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {

            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }

            //Declare singleton definitions to be later injected using by inject()
            //This view model is declared singleton to be used across multiple fragments
            viewModel {
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }

            // ReminderDataSource
            // TODO: Declare the local data source as a single to be injected in the repository
            // single { RemindersLocalRepository(get()) }

            // https://knowledge.udacity.com/questions/734982
            single<ReminderDataSource> { RemindersLocalRepository(get()) }


            single { LocalDB.createRemindersDao(this@MyApp) }

        }

        /**
         * From your Application class you can use the startKoin function and inject the Android context with androidContext
         * Reference: https://insert-koin.io/docs/reference/koin-android/start
         */
        startKoin {

            // use Android logger - Level.INFO by default
            androidLogger()

            // Inject | Reference Android context
            androidContext(this@MyApp)

            // Load modules
            modules(listOf(myModule))

        }

    }
}
