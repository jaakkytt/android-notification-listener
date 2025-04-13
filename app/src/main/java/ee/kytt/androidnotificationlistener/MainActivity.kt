package ee.kytt.androidnotificationlistener

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ee.kytt.androidnotificationlistener.service.NotificationSyncWorker
import ee.kytt.androidnotificationlistener.ui.theme.AndroidNotificationListenerTheme
import java.util.concurrent.TimeUnit.MINUTES

const val PREF_SYNC_ENABLED = "sync_enabled"
const val SYNC_WORK_NAME = "notification_sync"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidNotificationListenerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NotificationAccessUI(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationAccessUI(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", ComponentActivity.MODE_PRIVATE)

    val syncEnabledState = remember { mutableStateOf(isSyncEnabled(context)) }

    val savedUrl = remember { mutableStateOf(prefs.getString("callback_url", "") ?: "") }
    var urlText by remember { mutableStateOf(TextFieldValue(savedUrl.value)) }

    val savedPattern = remember { mutableStateOf(prefs.getString("package_pattern", "") ?: "") }
    var patternText by remember { mutableStateOf(TextFieldValue(savedPattern.value)) }

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = urlText,
            onValueChange = { urlText = it },
            label = {
                if (savedUrl.value.isNotEmpty()) {
                    Text("Callback URL")
                } else {
                    Text("Callback URL not set")
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    val newUrl = urlText.text
                    prefs.edit() { putString("callback_url", urlText.text) }
                    savedUrl.value = newUrl
                    Toast.makeText(context, "Url saved", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Save URL")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = patternText,
            onValueChange = { patternText = it },
            label = {
                if (savedPattern.value.isNotEmpty()) {
                    Text("Monitor matching applications")
                } else {
                    Text("Monitor all applications")
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    val newPattern = patternText.text
                    prefs.edit() { putString("package_pattern", patternText.text) }
                    savedPattern.value = newPattern
                    Toast.makeText(context, "Pattern saved", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Save pattern")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enable Notification Access")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                toggleBackgroundSync(context)
                syncEnabledState.value = !syncEnabledState.value

                val message = if (syncEnabledState.value) {
                    "Background sync enabled"
                } else {
                    "Background sync disabled"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            val label = if (syncEnabledState.value) "Disable Background Sync" else "Enable Background Sync"
            Text(label)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                triggerOneTimeNotificationSync(context)
                Toast.makeText(context, "Sync started", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sync Failed Notifications Now")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Latest Notification", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(4.dp))

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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationAccessUIPreview() {
    AndroidNotificationListenerTheme {
        NotificationAccessUI()
    }
}

fun isSyncEnabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean(PREF_SYNC_ENABLED, false)
}

fun toggleBackgroundSync(context: Context) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val currentlyEnabled = isSyncEnabled(context)

    if (currentlyEnabled) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
    } else {
        val request = PeriodicWorkRequestBuilder<NotificationSyncWorker>(15, MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    prefs.edit() { putBoolean(PREF_SYNC_ENABLED, !currentlyEnabled) }
}

fun triggerOneTimeNotificationSync(context: Context) {
    val request = OneTimeWorkRequestBuilder<NotificationSyncWorker>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "manual_sync",
        ExistingWorkPolicy.KEEP,
        request
    )
}
