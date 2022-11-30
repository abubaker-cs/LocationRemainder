package com.udacity.project4.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.utils.SingleLiveEvent

/**
 * Base class for View Models to declare the common LiveData objects in one place
 */
abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {

    // navigationCommand is used to navigate between fragments
    val navigationCommand: SingleLiveEvent<NavigationCommand> = SingleLiveEvent()

    // showErrorMessage is used to show error messages
    val showErrorMessage: SingleLiveEvent<String> = SingleLiveEvent()

    // showSnackBar | showSnackBarInt are used to show message using a snackbar
    val showSnackBar: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()

    // showToast is used to show a toast message
    val showToast: SingleLiveEvent<String> = SingleLiveEvent()

    // showLoading is used to show a loading spinner
    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()

    // showNoData is used to show a message when there is no data
    val showNoData: MutableLiveData<Boolean> = MutableLiveData()

}
