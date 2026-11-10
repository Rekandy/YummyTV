package su.afk.yummy.tv.feature.settings.view.category

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.domain.library.model.ExternalService
import su.afk.yummy.tv.feature.settings.SettingsState
import su.afk.yummy.tv.feature.settings.presentation.R
import su.afk.yummy.tv.feature.settings.utils.restoreCategoryFocusOnLeft
import su.afk.yummy.tv.feature.settings.view.AboutRow
import su.afk.yummy.tv.feature.settings.view.SettingsDivider
import su.afk.yummy.tv.feature.settings.view.SettingsSectionTitle

@Composable
internal fun SettingsTvListSyncContent(
    state: SettingsState.State,
    tabFocusRequester: FocusRequester,
    tabContentFocusRequester: FocusRequester,
    onEvent: (SettingsState.Event) -> Unit,
) {
    val malFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            onEvent(SettingsState.Event.ListSyncFileSelected(it.toString(), ExternalService.MAL))
        }
    }

    val shikimoriFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            onEvent(SettingsState.Event.ListSyncFileSelected(it.toString(), ExternalService.SHIKIMORI))
        }
    }

    SettingsSectionTitle(text = stringResource(R.string.settings_list_sync_import_section))

    AboutRow(
        label = stringResource(R.string.settings_list_sync_import_mal_title),
        hint = if (state.isListSyncLoading) {
            stringResource(R.string.settings_list_sync_loading_hint)
        } else {
            stringResource(R.string.settings_list_sync_import_mal_hint)
        },
        onClick = {
            if (!state.isListSyncLoading && !state.isListSyncImporting) {
                runCatching { malFilePicker.launch("*/*") }
            }
        },
        modifier = Modifier
            .focusRequester(tabContentFocusRequester)
            .restoreCategoryFocusOnLeft(tabFocusRequester),
    )

    SettingsDivider()

    AboutRow(
        label = stringResource(R.string.settings_list_sync_import_shiki_title),
        hint = if (state.isListSyncLoading) {
            stringResource(R.string.settings_list_sync_loading_hint)
        } else {
            stringResource(R.string.settings_list_sync_import_shiki_hint)
        },
        onClick = {
            if (!state.isListSyncLoading && !state.isListSyncImporting) {
                runCatching { shikimoriFilePicker.launch("*/*") }
            }
        },
    )

    SettingsDivider()

    SettingsSectionTitle(text = stringResource(R.string.settings_list_sync_export_section))

    AboutRow(
        label = stringResource(R.string.settings_list_sync_export_mal_title),
        hint = stringResource(R.string.settings_list_sync_export_mal_hint),
        onClick = {
            if (!state.isListSyncLoading && !state.isListSyncExporting) {
                onEvent(SettingsState.Event.ListSyncExportRequested(ExternalService.MAL))
            }
        },
    )

    SettingsDivider()

    AboutRow(
        label = stringResource(R.string.settings_list_sync_export_shiki_title),
        hint = stringResource(R.string.settings_list_sync_export_shiki_hint),
        onClick = {
            if (!state.isListSyncLoading && !state.isListSyncExporting) {
                onEvent(SettingsState.Event.ListSyncExportRequested(ExternalService.SHIKIMORI))
            }
        },
    )
}
