package sfu.namnguyen.myruns

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryFragment : Fragment() {

    private lateinit var exerciseViewModel: ExerciseEntryViewModel
    private lateinit var entryAdapter: EntryListAdapter
    private lateinit var deleteAllButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.history_recycler_view)

        deleteAllButton = view.findViewById(R.id.button_delete_all)

        val dao = ExerciseEntryDatabase.getInstance(requireContext()).exerciseEntryDao
        val repository = ExerciseEntryRepository(dao)
        val viewModelFactory = ExerciseEntryViewModelFactory(repository)

        exerciseViewModel = ViewModelProvider(this, viewModelFactory)[ExerciseEntryViewModel::class.java]

        val clickListener: (ExerciseEntry) -> Unit = { entry ->
            if (entry.inputType == 0) {
                val intent = Intent(requireContext(), DisplayEntryActivity::class.java)
                intent.putExtra("ENTRY_ID_KEY", entry.id)
                startActivity(intent)
            } else {
                val intent = Intent(requireContext(), MapDisplayActivity::class.java)
                intent.putExtra(StartFragment.INPUT_TYPE_KEY, "History")
                intent.putExtra("ENTRY_ID_KEY", entry.id)
                startActivity(intent)
            }
        }

        entryAdapter = EntryListAdapter(clickListener)
        recyclerView.adapter = entryAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val unitValue = sharedPrefs.getString("unit_preference", "metric")
        entryAdapter.isMetric = unitValue == "metric" // true if metric, false if imperial

        exerciseViewModel.allEntriesLiveData.observe(viewLifecycleOwner) { entries ->
            entryAdapter.submitList(entries)
        }

        deleteAllButton.setOnClickListener {
            // Show confirmation dialog
            AlertDialog.Builder(requireContext())
                .setTitle("Delete All History")
                .setMessage("Are you sure you want to delete all entries? This cannot be undone.")
                .setPositiveButton("Yes") { _, _ ->
                    // User confirmed, perform deletion
                    exerciseViewModel.deleteAllEntries()
                }
                .setNegativeButton("No", null)
                .show()
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        // check preferences every time the fragment is resumed
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val unitValue = sharedPrefs.getString("unit_preference", "metric")

        val newIsMetric = (unitValue == "metric")

        // check if the setting has actually changed
        if (entryAdapter.isMetric != newIsMetric) {
            entryAdapter.isMetric = newIsMetric

            entryAdapter.notifyDataSetChanged()
        }
    }
}