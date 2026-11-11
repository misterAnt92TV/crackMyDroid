package com.crackmydroid.desktop
import com.crackmydroid.ui.icons.AppIcons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.DeviceSessionSource
import com.crackmydroid.shared.domain.model.scansnapshot.SnapshotMetadata
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.ui.components.button.AppButton
import com.crackmydroid.ui.components.button.ButtonVariant
import com.crackmydroid.ui.components.card.AppCard
import com.crackmydroid.ui.components.text.AppText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun DesktopToolbarComponent(
    device: ConnectedDevice,
    dataSource: DeviceSessionSource,
    snapshotStatus: String?,
    snapshotMetadata: SnapshotMetadata?,
    onOpenAdbSettings: () -> Unit,
    onChangeDevice: () -> Unit,
    onRescan: () -> Unit,
) {
    val strings = LocalStrings.current
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = AppIcons.Filled.Devices,
                    contentDescription = null,
                )
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = device.model ?: device.serial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    AppText(
                        text = "${device.serial} • ${device.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AppText(
                        text = buildSourceLabel(strings, dataSource, snapshotMetadata),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    snapshotStatus?.let {
                        AppText(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.padding(start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppButton(
                    text = strings.desktopAdbSettings,
                    onClick = onOpenAdbSettings,
                    variant = ButtonVariant.Tonal,
                    leadingIcon = AppIcons.Filled.Edit,
                    fullWidth = false,
                )
                AppButton(
                    text = strings.desktopChangeDevice,
                    onClick = onChangeDevice,
                    variant = ButtonVariant.Tonal,
                    leadingIcon = AppIcons.Filled.SwapHoriz,
                    fullWidth = false,
                )
                AppButton(
                    text = strings.desktopRescan,
                    onClick = onRescan,
                    leadingIcon = AppIcons.Filled.Refresh,
                    fullWidth = false,
                )
            }
        }
    }
}

private fun buildSourceLabel(
    strings: Strings,
    dataSource: DeviceSessionSource,
    snapshotMetadata: SnapshotMetadata?,
): String {
    val sourceLabel = when (dataSource) {
        DeviceSessionSource.ADB -> strings.desktopAdbSource
        DeviceSessionSource.SNAPSHOT -> strings.desktopSnapshotSource
    }
    if (snapshotMetadata == null) return strings.desktopDataSourceLabel.format(sourceLabel)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val timestamp = Instant.ofEpochMilli(snapshotMetadata.createdAtEpochMs)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
    return "${strings.desktopDataSourceLabel.format(sourceLabel)} • $timestamp • ${snapshotMetadata.sourceAppVersion}"
}
