package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EnableNotificationsButton(
    context: Context,
    modifier: Modifier
) {
    Button(
        onClick = {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            context.startActivity(intent)
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Text("Enable Notification Access")
    }
}
