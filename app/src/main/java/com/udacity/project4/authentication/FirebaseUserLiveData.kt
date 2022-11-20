package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * This class observes the current FirebaseUser. If there is no logged in user, FirebaseUser will
 * be null.
 *
 * Note that onActive() and onInactive() will get triggered when the configuration changes (for
 * example when the device is rotated). This may be undesirable or expensive depending on the
 * nature of your LiveData object, but is okay for this purpose since we are only adding and
 * removing the authStateListener.
 */
class FirebaseUserLiveData : LiveData<FirebaseUser?>() {

    // I am obtaining an instance of the FirebaseAuth class by calling getInstance() method.
    //
    // Theory:
    // Firebase Auth enables you to subscribe in realtime to this state via a Stream . Once called,
    // the stream provides an immediate event of the user's current authentication state, and then
    // provides subsequent events whenever the authentication state changes.
    private val firebaseAuth = FirebaseAuth.getInstance()

    // I am setting the value of this FireUserLiveData object by hooking it up to equal the value of
    // the current FirebaseUser. It can utilize the FirebaseAuth.AuthStateListener callback to get
    // updates on the current Firebase user logged into the app.
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->

        // Using the FirebaseAuth instance instantiated at the beginning of the class to get an
        // entry point into the Firebase Authentication SDK the app is using. FirebaseAuth class
        // is used to query for the current user.
        value = firebaseAuth.currentUser
    }

    // When this object has an active observer, start observing the FirebaseAuth state to see if
    // there is currently a logged in user.
    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    // When this object no longer has an active observer, stop observing the FirebaseAuth state to
    // prevent memory leaks.
    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

}
