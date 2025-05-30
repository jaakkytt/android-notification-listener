package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ee.kytt.androidnotificationlistener.Constants.SETTINGS_INTENT
import ee.kytt.androidnotificationlistener.Constants.SETTINGS_URI
import ee.kytt.androidnotificationlistener.R

@Composable
fun EnableNotificationsButton(
    context: Context,
    modifier: Modifier
) {
    var enabled by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentContext by rememberUpdatedState(context)

    var labelNotificationAccessEnabled = stringResource(R.string.notification_access_enabled)
    var labelNotificationAccessDisabled = stringResource(R.string.notification_access_disabled)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = isNotificationListenerEnabled(currentContext)
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                enabled = isNotificationListenerEnabled(context)
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(SETTINGS_URI),
            false,
            observer
        )
        onDispose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    Button(
        onClick = {
            context.startActivity(Intent(SETTINGS_INTENT))
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (enabled) labelNotificationAccessEnabled else labelNotificationAccessDisabled,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Switch(
                checked = enabled,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

fun isNotificationListenerEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, SETTINGS_URI)
    return flat?.split(":")?.any { it.contains(pkgName) } == true
}
