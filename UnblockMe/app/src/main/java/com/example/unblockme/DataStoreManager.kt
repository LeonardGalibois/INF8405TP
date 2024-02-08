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

    suspend fun setHighscore(puzzleNumber: Int, newScore: Int) {
        dataStore.edit { highscores ->
            highscores[getPreferencesKey(puzzleNumber)] = newScore
        }
    }

    suspend fun getHighscore(puzzleNumber: Int): Int {
        val preferencesKey = getPreferencesKey(puzzleNumber)
        val highscore : Flow<Int> = dataStore.data.map { highscores ->
            highscores[preferencesKey] ?: 0
        }
        return highscore.first()
    }
}
