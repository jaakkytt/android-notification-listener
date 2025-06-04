package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.util.Log
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
import androidx.core.content.edit
import ee.kytt.androidnotificationlistener.Constants.PREFS_NAME

@Composable
fun TextField(
    context: Context,
    prefKey: String,
    labelSet: String,
    labelNotSet: String,
    description: String
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, ComponentActivity.MODE_PRIVATE)
    var textValue by remember {
        mutableStateOf(prefs.getString(prefKey, "") ?: "")
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = {
            textValue = it
            prefs.edit() { putString(prefKey, textValue) }
            Log.d("TextField", "Text value is set to: $textValue")
        },
        label = {
            Text(if (textValue.isNotEmpty()) labelSet else labelNotSet)
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Description(text = description)
}
