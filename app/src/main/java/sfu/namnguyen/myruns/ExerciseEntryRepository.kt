package sfu.namnguyen.myruns

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ExerciseEntryRepository(private val dao: ExerciseEntryDao) {
    val allEntries: Flow<List<ExerciseEntry>> = dao.getAllEntries()

    fun insertEntry(entry: ExerciseEntry) {
        CoroutineScope(IO).launch {
            dao.insertEntry(entry)
        }
    }

    fun deleteEntry(entry: ExerciseEntry) {
        CoroutineScope(IO).launch {
            dao.deleteEntry(entry)
        }
    }

    fun getEntryById(entryId: Long, callback: (ExerciseEntry?) -> Unit) {
        CoroutineScope(IO).launch {
            val entry = dao.getEntryById(entryId)
            callback(entry)
        }
    }
}
