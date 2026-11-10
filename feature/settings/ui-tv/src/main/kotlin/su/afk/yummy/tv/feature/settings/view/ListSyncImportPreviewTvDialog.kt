package su.afk.yummy.tv.feature.settings.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.domain.library.model.ExternalService
import su.afk.yummy.tv.domain.library.model.ImportConflictStrategy
import su.afk.yummy.tv.feature.settings.SettingsState
import su.afk.yummy.tv.feature.settings.presentation.R

@Composable
internal fun ListSyncImportPreviewTvDialog(
    state: SettingsState.State,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onStrategyChanged: (ImportConflictStrategy) -> Unit,
    onSkipUnmatchedToggled: (Boolean) -> Unit,
) {
    val confirmFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { confirmFocusRequester.requestFocus() }

    val matchedCount = state.listSyncImportPreview.count { it.localAnimeId != null }
    val conflictCount = state.listSyncImportPreview.count { it.hasConflict }
    val unmatchedCount = state.listSyncImportPreview.size - matchedCount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_list_sync_import_preview_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.listSyncImportService?.let { service ->
                    val serviceName = when (service) {
                        ExternalService.MAL -> "MyAnimeList"
                        ExternalService.SHIKIMORI -> "Shikimori"
                    }
                    Text(
                        text = stringResource(R.string.settings_list_sync_import_source, serviceName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = stringResource(R.string.settings_list_sync_import_found_count, state.listSyncImportPreview.size),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.settings_list_sync_import_matched_count, matchedCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (unmatchedCount > 0) {
                    Text(
                        text = stringResource(R.string.settings_list_sync_import_unmatched_count, unmatchedCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (conflictCount > 0) {
                    Text(
                        text = stringResource(R.string.settings_list_sync_import_conflict_count, conflictCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_list_sync_import_conflict_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.listSyncConflictStrategy == ImportConflictStrategy.UPDATE,
                        onClick = { onStrategyChanged(ImportConflictStrategy.UPDATE) },
                        label = { Text(stringResource(R.string.settings_list_sync_import_strategy_update)) },
                    )
                    FilterChip(
                        selected = state.listSyncConflictStrategy == ImportConflictStrategy.SKIP,
                        onClick = { onStrategyChanged(ImportConflictStrategy.SKIP) },
                        label = { Text(stringResource(R.string.settings_list_sync_import_strategy_skip)) },
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSkipUnmatchedToggled(!state.listSyncSkipUnmatched) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = state.listSyncSkipUnmatched,
                        onCheckedChange = onSkipUnmatchedToggled,
                    )
                    Text(
                        text = stringResource(R.string.settings_list_sync_import_skip_unmatched),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !state.isListSyncImporting,
                modifier = Modifier.focusRequester(confirmFocusRequester),
            ) {
                Text(
                    if (state.isListSyncImporting) {
                        stringResource(R.string.settings_list_sync_import_btn_importing)
                    } else {
                        stringResource(R.string.settings_list_sync_import_btn_import)
                    },
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isListSyncImporting,
            ) {
                Text(stringResource(R.string.settings_list_sync_btn_cancel))
            }
        },
    )
}
