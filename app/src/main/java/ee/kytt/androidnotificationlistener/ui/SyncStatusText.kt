package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun SyncStatusText(
    context: Context
) {
    val prefs = context.getSharedPreferences("app_prefs", ComponentActivity.MODE_PRIVATE)
    val latestTitle = remember { mutableStateOf(prefs.getString("latestTitle", "") ?: "") }
    val latestPackage = remember { mutableStateOf(prefs.getString("latestPackageName", "No attempts yet") ?: "No attempts yet") }
    val latestStatus = remember { mutableStateOf(prefs.getString("latestStatus", "No attempts yet") ?: "No attempts yet") }
    val listener = remember {
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "latestTitle" -> latestTitle.value = prefs.getString("latestTitle", "") ?: ""
                "latestPackageName" -> latestPackage.value = prefs.getString("latestPackageName", "No attempts yet") ?: "No attempts yet"
                "latestStatus" -> latestStatus.value = prefs.getString("latestStatus", "No attempts yet") ?: "No attempts yet"
            }
        }
    }

    DisposableEffect(Unit) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val isSuccess = latestStatus.value.startsWith("Success", ignoreCase = true)
    val statusColor = if (isSuccess) Color.Green else Color.Red

    Text(
        text = latestPackage.value,
        color = statusColor,
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        text = latestTitle.value,
        color = statusColor,
        style = MaterialTheme.typography.bodyMedium
    )
}
