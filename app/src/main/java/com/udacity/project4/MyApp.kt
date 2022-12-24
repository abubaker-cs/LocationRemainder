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

@Suppress("USELESS_CAST")
class MyApp : Application() {

    override fun onCreate() {

        super.onCreate()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {

            /**
             * ViewModels: SaveReminderViewModel + RemindersListViewModel
             */

            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }

            /**
             * Singletons: LocalDB + RemindersLocalRepository
             */

            //Declare singleton definitions to be later injected using by inject()
            viewModel {
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }

            // RemindersLocalRepository
            single { RemindersLocalRepository(get()) }

            // ReminderDataSource
            single<ReminderDataSource> { get<RemindersLocalRepository>() }

            single { LocalDB.createRemindersDao(this@MyApp) }

            /**
             * Factory - It will create a new instance of MainRepositoryImpl every time it is requested
             */
//            factory {
//
//                // If we have two view models that need the same repository, we can use the single
//                // keyword to create a single instance of the repository and share it across the application
//                RemindersLocalRepository(get())
//
//            }

        }

        /**
         * From your Application class you can use the startKoin function and inject the Android context with androidContext
         * Reference: https://insert-koin.io/docs/reference/koin-android/start
         */
        startKoin {

            // Logger
            androidLogger()

            // Inject | Reference Android context
            androidContext(this@MyApp)

            // Load modules
            modules(listOf(myModule))

        }

    }
}
