package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ee.kytt.androidnotificationlistener.Constants.PREF_CALLBACK_TOKEN
import ee.kytt.androidnotificationlistener.Constants.PREF_CALLBACK_URL
import ee.kytt.androidnotificationlistener.Constants.PREF_PACKAGE_PATTERN
import ee.kytt.androidnotificationlistener.R
import ee.kytt.androidnotificationlistener.ui.element.ContentGroup
import ee.kytt.androidnotificationlistener.ui.element.TextField

@Composable
fun SettingsScreen(
    context: Context,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val divModifier = Modifier.padding(vertical = 16.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { onBack?.invoke() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        ContentGroup(title = stringResource(R.string.notifications)) {
            EnableNotificationsButton(context)

            HorizontalDivider(modifier = divModifier)

            AutoRevokeStatusButton(context)
        }

        ContentGroup(title = stringResource(R.string.filter)) {
            TextField(
                context = context,
                prefKey = PREF_PACKAGE_PATTERN,
                labelSet = stringResource(R.string.monitor_matching_apps),
                labelNotSet = stringResource(R.string.monitor_all_apps),
                description = stringResource(R.string.monitor_matching_apps_description)
            )
        }

        ContentGroup(title = stringResource(R.string.remote_server)) {
            BackgroundSyncButton(context)

            HorizontalDivider(modifier = divModifier)

            TextField(
                context = context,
                prefKey = PREF_CALLBACK_URL,
                labelSet = stringResource(R.string.callback_url),
                labelNotSet = stringResource(R.string.callback_url_not_set),
                description = stringResource(R.string.callback_url_description)
            )

            HorizontalDivider(modifier = divModifier)

            TextField(
                context = context,
                prefKey = PREF_CALLBACK_TOKEN,
                labelSet = stringResource(R.string.callback_token_set),
                labelNotSet = stringResource(R.string.callback_token_not_set),
                description = stringResource(R.string.callback_token_description)
            )
        }
    }
}
