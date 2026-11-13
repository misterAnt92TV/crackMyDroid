package com.crackmydroid.desktop
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.ui.components.button.AppButton
import com.crackmydroid.ui.components.button.ButtonVariant
import com.crackmydroid.ui.components.card.AppCard
import com.crackmydroid.ui.components.progress.AppProgressIndicator
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.components.text.AppTextField
import com.crackmydroid.ui.icons.AppIcons
import com.crackmydroid.ui.theme.CrackMyDroidTheme

@Composable
internal fun DevicePickerContent(
    adbPath: String,
    adbPathConfigured: Boolean,
    onAdbPathChange: (String) -> Unit,
    devices: List<ConnectedDevice>,
    loading: Boolean,
    error: String?,
    onOpenAdbSettings: () -> Unit,
    onRefresh: () -> Unit,
    onSelect: (ConnectedDevice) -> Unit,
) {
    val strings = LocalStrings.current
    val spacing = CrackMyDroidTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(spacing.large),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = spacing.huge),
            verticalArrangement = Arrangement.spacedBy(spacing.default),
        ) {
            AppText(
                text = strings.desktopAdbPickerTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            AppText(
                text = strings.desktopAdbPickerDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!adbPathConfigured) {
                AppTextField(
                    value = adbPath,
                    onValueChange = onAdbPathChange,
                    label = strings.desktopAdbPathLabel,
                    singleLine = true,
                )
            } else {
                AppText(
                    text = strings.desktopAdbPathConfigured,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.extraSmall),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppButton(
                    text = strings.desktopDetectDevices,
                    onClick = onRefresh,
                    leadingIcon = AppIcons.Filled.Refresh,
                    fullWidth = false,
                )
                AppButton(
                    text = strings.desktopAdbSettings,
                    onClick = onOpenAdbSettings,
                    variant = ButtonVariant.Tonal,
                    leadingIcon = AppIcons.Filled.Settings,
                    fullWidth = false,
                )
                if (loading) {
                    AppProgressIndicator(strings.desktopDetectDevices, modifier = Modifier.size(26.dp))
                }
            }
            error?.let {
                AppText(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            AppCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (devices.isEmpty() && !loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.large),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppText(
                            text = strings.desktopNoDevices,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(spacing.small),
                    ) {
                        items(devices) { device ->
                            val selectable = device.state == "device"
                            AppCard(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = selectable,
                                onClick = { onSelect(device) },
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(spacing.default),
                                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                                ) {
                                    AppText(
                                        text = device.model ?: device.serial,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    AppText(
                                        text = "${strings.desktopSerialLabel}: ${device.serial}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    AppText(
                                        text = "${strings.desktopStateLabel}: ${device.state}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selectable) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        },
                                    )
                                    AppText(
                                        text = listOfNotNull(
                                            device.product?.let { "${strings.desktopProductLabel}: $it" },
                                            device.device?.let { "${strings.desktopDeviceLabel}: $it" },
                                            device.transportId?.let { "${strings.desktopTransportLabel}: $it" },
                                        ).joinToString("  |  "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (!selectable) {
                                        AppText(
                                            text = when (device.state) {
                                                "offline" -> strings.desktopDeviceOffline
                                                "unauthorized" -> strings.desktopDeviceUnauthorized
                                                else -> strings.desktopDeviceNotReady
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
