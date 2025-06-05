package ee.kytt.androidnotificationlistener.ui.element

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ee.kytt.androidnotificationlistener.ui.theme.Green
import ee.kytt.androidnotificationlistener.ui.theme.Red

@Composable
fun SettingSwitch(
    title: String,
    checked: Boolean,
    description: String = "",
    inverted: Boolean = false,
    onClick: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (inverted) Red else Green,
                uncheckedThumbColor = if (inverted) Green else Red,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            ),
        )
    }
    Description(text = description)
}
