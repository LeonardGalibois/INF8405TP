import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "highscores")
class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val puzzlePrefix = "puzzle"
    }

    private fun getPreferencesKey(puzzleNumber: Int): Preferences.Key<Int> {
        return intPreferencesKey("${puzzlePrefix}${puzzleNumber}")
    }

    // Set new high score if new score is better
    suspend fun setHighscore(puzzleNumber: Int, newScore: Int) {
        if (newScore >= getHighscore(puzzleNumber)) return
        dataStore.edit { highscores ->
            highscores[getPreferencesKey(puzzleNumber)] = newScore
        }
    }

    // Return high score for each puzzle
    suspend fun getHighscore(puzzleNumber: Int): Int {
        val preferencesKey = getPreferencesKey(puzzleNumber)
        val highscore : Flow<Int> = dataStore.data.map { highscores ->
            highscores[preferencesKey] ?: 0
        }
        return highscore.first()
    }
}
