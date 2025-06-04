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
import ee.kytt.androidnotificationlistener.Constants.PREF_CALLBACK_URL
import ee.kytt.androidnotificationlistener.R

@Composable
fun CallbackUrlField(
    context: Context,
    modifier: Modifier
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, ComponentActivity.MODE_PRIVATE)
    val savedUrl = remember { mutableStateOf(prefs.getString(PREF_CALLBACK_URL, "") ?: "") }
    var urlText by remember { mutableStateOf(TextFieldValue(savedUrl.value)) }

    var urlSetLabel = stringResource(R.string.callback_url)
    var notSetLabel = stringResource(R.string.callback_url_not_set)

    OutlinedTextField(
        value = urlText,
        onValueChange = {
            urlText = it
            prefs.edit() { putString(PREF_CALLBACK_URL, urlText.text) }
            savedUrl.value = urlText.text
        },
        label = {
            Text(if (savedUrl.value.isNotEmpty()) urlSetLabel else notSetLabel)
        },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )

    Description(text = stringResource(R.string.callback_url_description))
}
