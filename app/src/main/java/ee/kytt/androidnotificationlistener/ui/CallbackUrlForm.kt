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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import ee.kytt.androidnotificationlistener.Constants.PREFS_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_CALLBACK_URL

@Composable
fun CallbackUrlForm(
    context: Context,
    modifier: Modifier
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, ComponentActivity.MODE_PRIVATE)
    val savedUrl = remember { mutableStateOf(prefs.getString(PREF_CALLBACK_URL, "") ?: "") }
    var urlText by remember { mutableStateOf(TextFieldValue(savedUrl.value)) }

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
        modifier = modifier.fillMaxWidth()
    )

    Spacer(modifier = modifier.height(8.dp))

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = {
                val newUrl = urlText.text
                prefs.edit() { putString(PREF_CALLBACK_URL, urlText.text) }
                savedUrl.value = newUrl
                Toast.makeText(context, "Url saved", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text("Save URL")
        }
    }
}
