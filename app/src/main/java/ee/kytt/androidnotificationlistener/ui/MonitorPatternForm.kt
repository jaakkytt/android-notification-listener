package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import ee.kytt.androidnotificationlistener.Constants.PREFS_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_PACKAGE_PATTERN
import ee.kytt.androidnotificationlistener.R

@Composable
fun MonitorPatternForm(
    context: Context,
    modifier: Modifier
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, ComponentActivity.MODE_PRIVATE)
    val savedPattern = remember { mutableStateOf(prefs.getString(PREF_PACKAGE_PATTERN, "") ?: "") }
    var patternText by remember { mutableStateOf(TextFieldValue(savedPattern.value)) }

    var labelMonitorAllApps = stringResource(R.string.monitor_all_apps)
    var labelMonitorMatchingApps = stringResource(R.string.monitor_matching_apps)
    var labelSavePattern = stringResource(R.string.save_pattern)
    var labelPatternSaved = stringResource(R.string.pattern_saved)

    OutlinedTextField(
        value = patternText,
        onValueChange = { patternText = it },
        label = {
            Text(if (savedPattern.value.isNotEmpty()) labelMonitorMatchingApps else labelMonitorAllApps)
        },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )

    Spacer(modifier = modifier.height(8.dp))

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = {
                val newPattern = patternText.text
                prefs.edit() { putString(PREF_PACKAGE_PATTERN, patternText.text) }
                savedPattern.value = newPattern
                Toast.makeText(context, labelPatternSaved, Toast.LENGTH_SHORT).show()
            }
        ) {
            Text(labelSavePattern)
        }
    }
}
