package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.content.edit
import ee.kytt.androidnotificationlistener.Constants.PREFS_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_PACKAGE_PATTERN
import ee.kytt.androidnotificationlistener.R

@Composable
fun MonitorPatternField(
    context: Context,
    modifier: Modifier
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, ComponentActivity.MODE_PRIVATE)
    val savedPattern = remember { mutableStateOf(prefs.getString(PREF_PACKAGE_PATTERN, "") ?: "") }
    var patternText by remember { mutableStateOf(TextFieldValue(savedPattern.value)) }

    var allLabel = stringResource(R.string.monitor_all_apps)
    var matchingLabel = stringResource(R.string.monitor_matching_apps)

    OutlinedTextField(
        value = patternText,
        onValueChange = {
            patternText = it
            prefs.edit() { putString(PREF_PACKAGE_PATTERN, patternText.text) }
            savedPattern.value = patternText.text
        },
        label = {
            Text(if (savedPattern.value.isNotEmpty()) matchingLabel else allLabel)
        },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )

    Description(text = stringResource(R.string.monitor_matching_apps_description))
}
