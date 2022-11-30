package com.udacity.project4.base

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : Fragment() {
    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val baseViewModel: BaseViewModel

    override fun onStart() {

        super.onStart()

        // Error Message Observer
        baseViewModel.showErrorMessage.observe(this, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })

        // Toast Message Observer
        baseViewModel.showToast.observe(this, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })

        // SnackBar Message Observer
        baseViewModel.showSnackBar.observe(this, Observer {
            Snackbar.make(this.requireView(), it, Snackbar.LENGTH_LONG).show()
        })

        baseViewModel.showSnackBarInt.observe(this, Observer {
            Snackbar.make(this.requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        })

        // Navigation Observer
        baseViewModel.navigationCommand.observe(this, Observer { command ->
            when (command) {

                // Navigate to the next fragment
                is NavigationCommand.To -> findNavController().navigate(command.directions)

                // Navigate to the next fragment and pop the back stack
                is NavigationCommand.Back -> findNavController().popBackStack()

                // Navigate to the next fragment and pop the back stack to the destination
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        })

    }
}
