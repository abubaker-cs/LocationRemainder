package com.udacity.project4.locationreminders.data.dto


/**
 * A sealed class that encapsulates successful outcome with a value of type [T]
 * or a failure with message and statusCode
 */
sealed class Result<out T : Any> {

    //  Success
    data class Success<out T : Any>(val data: T) : Result<T>()

    //  Error
    data class Error(val message: String?, val statusCode: Int? = null) : Result<Nothing>()

}
