package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.*
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()

    // FragmentRemindersBinding is a generated class that contains all the views in the fragment_reminders.xml layout
    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        // binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reminders, container, false)
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)

        // Set the view model for databinding - this allows the bound layout access to all the data in the ViewModel
        binding.viewModel = _viewModel

        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        setDisplayHomeAsUpEnabled(false)

        // Set the title bar
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener {
            _viewModel.loadReminders()
            binding.refreshLayout.isRefreshing = false
        }

        // Return the root view
        return binding.root

    }

    // This function is called after onCreateView and after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()

        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }

    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    // Set up the recycler view
    private fun setupRecyclerView() {

        val adapter = RemindersListAdapter {}

        // setup the recycler view using the extension function
        binding.remindersRecyclerView.setup(adapter)

    }

    // Add the menu to the app bar
    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // If the user clicks the logout menu item, then log them out
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
                    FirebaseAuth.getInstance().signOut()
                    activity?.onBackPressed()
                }
            }
        }

        return super.onOptionsItemSelected(item)

    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        super.onCreateOptionsMenu(menu, inflater)

        // display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)

    }

    /**
     * LeakCanary detected a memory leak being caused by swipeRefreshLayout,
     * so I am trying to resolve this issue
     * Solution: https://stackoverflow.com/questions/56796929/memory-leak-with-swiperefreshlayout
     */
    override fun onPause() {
        binding.refreshLayout.isRefreshing = false
        binding.refreshLayout.isEnabled = false
        super.onPause()
    }

    override fun onStop() {
        binding.refreshLayout.isRefreshing = true
        binding.refreshLayout.isEnabled = true
        super.onStop()
    }


    // This will avoid memory leaks
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
