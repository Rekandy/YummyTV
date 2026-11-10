package su.afk.yummy.tv.feature.settings.view

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import su.afk.yummy.tv.domain.library.model.ExternalService
import su.afk.yummy.tv.feature.settings.presentation.R

@Composable
internal fun ListSyncExportConfirmTvDialog(
    count: Int,
    service: ExternalService?,
    isExporting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val confirmFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { confirmFocusRequester.requestFocus() }

    val serviceFormat = when (service) {
        ExternalService.MAL -> "MyAnimeList (XML)"
        ExternalService.SHIKIMORI -> "Shikimori (JSON)"
        null -> "XML"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_list_sync_export_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.settings_list_sync_export_message, count, serviceFormat),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isExporting,
                modifier = Modifier.focusRequester(confirmFocusRequester),
            ) {
                Text(
                    if (isExporting) {
                        stringResource(R.string.settings_list_sync_export_btn_exporting)
                    } else {
                        stringResource(R.string.settings_list_sync_export_btn_export)
                    },
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isExporting,
            ) {
                Text(stringResource(R.string.settings_list_sync_btn_cancel))
            }
        },
    )
}
