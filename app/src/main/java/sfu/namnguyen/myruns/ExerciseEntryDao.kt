package sfu.namnguyen.myruns

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseEntryDao {

    // insert a new exercise entry into the database
    @Insert
    suspend fun insertEntry(exerciseEntry: ExerciseEntry)

    // delete an existing exercise entry from the database
    @Delete
    suspend fun deleteEntry(exerciseEntry: ExerciseEntry)

    // get an exercise entry by its ID
    @Query("SELECT * FROM exercise_entries_table WHERE id = :key")
    suspend fun getEntryById(key: Long): ExerciseEntry?

    // get all exercise entries from the database as a Flow of List of ExerciseEntry
    @Query("SELECT * FROM exercise_entries_table ORDER BY id DESC")
    fun getAllEntries(): Flow<List<ExerciseEntry>>
}