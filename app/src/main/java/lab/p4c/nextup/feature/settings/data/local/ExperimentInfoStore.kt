package lab.p4c.nextup.feature.settings.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import lab.p4c.nextup.core.domain.experiment.model.ExperimentInfo
import lab.p4c.nextup.core.domain.experiment.port.ExperimentInfoRepository

private val Context.dataStore by preferencesDataStore("experiment_info")

class ExperimentInfoStore(private val context: Context) : ExperimentInfoRepository {

    private val KEY_NAME = stringPreferencesKey("name")
    private val KEY_AGE = intPreferencesKey("age")
    private val KEY_GENDER = stringPreferencesKey("gender")

    override suspend fun save(info: ExperimentInfo) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NAME] = info.name
            info.age.let { prefs[KEY_AGE] = it }
            info.gender.let { prefs[KEY_GENDER] = it }
        }
    }

    override suspend fun get(): ExperimentInfo? {
        val prefs = context.dataStore.data.map { it }.first()
        val name = prefs[KEY_NAME] ?: return null
        val age: Int = prefs[KEY_AGE] as Int
        val gender: String = prefs[KEY_GENDER] as String
        return ExperimentInfo(name, age, gender)
    }
}