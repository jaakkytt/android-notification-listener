package ee.kytt.androidnotificationlistener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ee.kytt.androidnotificationlistener.ui.SettingsScreen
import ee.kytt.androidnotificationlistener.ui.SyncNowButton
import ee.kytt.androidnotificationlistener.ui.SyncStatusText
import ee.kytt.androidnotificationlistener.ui.theme.AndroidNotificationListenerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidNotificationListenerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationAccessUI(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onSettingsClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.open_settings))
        }

        Spacer(modifier = Modifier.height(32.dp))

        SyncNowButton(context, Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(32.dp))

        Text(stringResource(R.string.sync_status), style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(4.dp))

        SyncStatusText(context)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationAccessUIPreview() {
    AndroidNotificationListenerTheme {
        MainScreen()
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showSettings = remember { mutableStateOf(false) }

    if (showSettings.value) {
        BackHandler {
            showSettings.value = false
        }
        SettingsScreen(
            context,
            modifier,
            onBack = { showSettings.value = false }
        )
    } else {
        NotificationAccessUI(
            modifier = modifier,
            onSettingsClick = { showSettings.value = true }
        )
    }
}
