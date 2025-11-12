package sfu.namnguyen.myruns

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData

class ExerciseEntryViewModel(private val repository: ExerciseEntryRepository) : ViewModel() {
    val allEntriesLiveData: LiveData<List<ExerciseEntry>> = repository.allEntries.asLiveData()

    fun insertEntry(entry: ExerciseEntry) {
        repository.insertEntry(entry)
    }

    fun deleteEntry(entry: ExerciseEntry) {
        repository.deleteEntry(entry)
    }

    fun getEntryById(entryId: Long, callback: (ExerciseEntry?) -> Unit) {
        repository.getEntryById(entryId, callback)
    }
}

class ExerciseEntryViewModelFactory(private val repository: ExerciseEntryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseEntryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class requested")
    }
}