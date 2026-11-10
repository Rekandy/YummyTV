package su.afk.yummy.tv.feature.settings.mobile.view.category

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.domain.library.model.ExternalService
import su.afk.yummy.tv.feature.settings.SettingsState
import su.afk.yummy.tv.feature.settings.mobile.view.ListSyncExportConfirmDialog
import su.afk.yummy.tv.feature.settings.mobile.view.ListSyncImportPreviewDialog
import su.afk.yummy.tv.feature.settings.mobile.view.ListSyncResultDialog
import su.afk.yummy.tv.feature.settings.mobile.view.SettingsMobileNavigationRow
import su.afk.yummy.tv.feature.settings.mobile.view.SettingsMobileSection
import su.afk.yummy.tv.feature.settings.presentation.R

@Composable
internal fun SettingsMobileListSyncContent(
    state: SettingsState.State,
    onEvent: (SettingsState.Event) -> Unit,
) {
    val malFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            onEvent(SettingsState.Event.ListSyncFileSelected(it.toString(), ExternalService.MAL))
        }
    }

    val shikimoriFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            onEvent(SettingsState.Event.ListSyncFileSelected(it.toString(), ExternalService.SHIKIMORI))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SettingsMobileSection(
            title = stringResource(R.string.settings_list_sync_import_section),
            subtitle = stringResource(R.string.settings_list_sync_import_section_hint),
        ) {
            SettingsMobileNavigationRow(
                label = stringResource(R.string.settings_list_sync_import_mal_title),
                hint = stringResource(R.string.settings_list_sync_import_mal_hint),
                icon = Icons.Outlined.Download,
                onClick = {
                    if (!state.isListSyncLoading && !state.isListSyncImporting) {
                        malFilePicker.launch("*/*")
                    }
                },
            )
            SettingsMobileNavigationRow(
                label = stringResource(R.string.settings_list_sync_import_shiki_title),
                hint = stringResource(R.string.settings_list_sync_import_shiki_hint),
                icon = Icons.Outlined.Download,
                onClick = {
                    if (!state.isListSyncLoading && !state.isListSyncImporting) {
                        shikimoriFilePicker.launch("*/*")
                    }
                },
            )
        }

        SettingsMobileSection(
            title = stringResource(R.string.settings_list_sync_export_section),
            subtitle = stringResource(R.string.settings_list_sync_export_section_hint),
        ) {
            SettingsMobileNavigationRow(
                label = stringResource(R.string.settings_list_sync_export_mal_title),
                hint = stringResource(R.string.settings_list_sync_export_mal_hint),
                icon = Icons.Outlined.Upload,
                onClick = {
                    if (!state.isListSyncLoading && !state.isListSyncExporting) {
                        onEvent(SettingsState.Event.ListSyncExportRequested(ExternalService.MAL))
                    }
                },
            )
            SettingsMobileNavigationRow(
                label = stringResource(R.string.settings_list_sync_export_shiki_title),
                hint = stringResource(R.string.settings_list_sync_export_shiki_hint),
                icon = Icons.Outlined.Upload,
                onClick = {
                    if (!state.isListSyncLoading && !state.isListSyncExporting) {
                        onEvent(SettingsState.Event.ListSyncExportRequested(ExternalService.SHIKIMORI))
                    }
                },
            )
        }
    }

    if (state.showListSyncImportDialog) {
        ListSyncImportPreviewDialog(
            state = state,
            onConfirm = { onEvent(SettingsState.Event.ListSyncImportConfirmed) },
            onDismiss = { onEvent(SettingsState.Event.ListSyncImportCancelled) },
            onStrategyChanged = {
                onEvent(SettingsState.Event.ListSyncConflictStrategyChanged(it))
            },
            onSkipUnmatchedToggled = {
                onEvent(SettingsState.Event.ListSyncSkipUnmatchedToggled(it))
            },
        )
    }

    if (state.showListSyncExportDialog) {
        ListSyncExportConfirmDialog(
            count = state.listSyncExportCount,
            service = state.listSyncExportService,
            isExporting = state.isListSyncExporting,
            onConfirm = { onEvent(SettingsState.Event.ListSyncExportConfirmed) },
            onDismiss = { onEvent(SettingsState.Event.ListSyncExportCancelled) },
        )
    }

    val lastResult = state.listSyncLastResult
    if (state.showListSyncResultDialog && lastResult != null) {
        ListSyncResultDialog(
            result = lastResult,
            onDismiss = { onEvent(SettingsState.Event.ListSyncResultDismissed) },
        )
    }
}
