package ee.kytt.androidnotificationlistener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import ee.kytt.androidnotificationlistener.ui.MainScreen
import ee.kytt.androidnotificationlistener.ui.SettingsScreen
import ee.kytt.androidnotificationlistener.ui.theme.AndroidNotificationListenerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidNotificationListenerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScreenHolder(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationAccessUIPreview() {
    AndroidNotificationListenerTheme {
        ScreenHolder()
    }
}

@Composable
fun ScreenHolder(modifier: Modifier = Modifier) {
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
        MainScreen(
            context = context,
            modifier = modifier,
            onSettingsClick = { showSettings.value = true }
        )
    }
}
