package com.crackmydroid.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import com.crackmydroid.shared.ui.AppPresentationDependencies
import com.crackmydroid.shared.ui.CrackMyDroidApp
import com.crackmydroid.ui.components.dialog.AppDialog
import com.crackmydroid.ui.components.text.AppText
import org.koin.mp.KoinPlatform

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val packageInventoryAccess = packageInventoryAccess(
            sdkInt = Build.VERSION.SDK_INT,
            queryAllPackagesPermissionGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && checkSelfPermission(
                Manifest.permission.QUERY_ALL_PACKAGES,
            ) == PackageManager.PERMISSION_GRANTED,
        )
        val disclosurePreferences =
            getSharedPreferences(DISCLOSURE_PREFERENCES, Context.MODE_PRIVATE)
        val disclosureSeen = disclosurePreferences.getBoolean(DISCLOSURE_SEEN_KEY, false)
        val presentationDependencies = KoinPlatform.getKoin().get<AppPresentationDependencies>()
        setContent {
            Surface {
                when (packageInventoryAccess) {
                    PackageInventoryAccess.Full -> {
                        CrackMyDroidApp(
                            dependencies = presentationDependencies,
                            systemMotionReductionEnabled = rememberSystemMotionReduction(),
                            onExitApp = ::finish,
                        )
                        var showDisclosure by remember { mutableStateOf(!disclosureSeen) }
                        if (showDisclosure) {
                            PackageInventoryDisclosure(
                                onAcknowledge = {
                                    disclosurePreferences.edit {
                                        putBoolean(DISCLOSURE_SEEN_KEY, true)
                                    }
                                    showDisclosure = false
                                },
                            )
                        }
                    }

                    PackageInventoryAccess.Unavailable -> PackageInventoryUnavailableDialog(
                        onClose = ::finish,
                    )
                }
            }
        }
    }

    private companion object {
        const val DISCLOSURE_PREFERENCES = "package_inventory_disclosure"
        const val DISCLOSURE_SEEN_KEY = "seen"
    }
}

@androidx.compose.runtime.Composable
private fun PackageInventoryDisclosure(onAcknowledge: () -> Unit) {
    AppDialog(
        title = stringResource(R.string.package_inventory_disclosure_title),
        onDismiss = onAcknowledge,
        confirmLabel = stringResource(R.string.package_inventory_disclosure_confirm),
        onConfirm = onAcknowledge,
    ) {
        AppText(stringResource(R.string.package_inventory_disclosure_body))
    }
}

@androidx.compose.runtime.Composable
private fun PackageInventoryUnavailableDialog(onClose: () -> Unit) {
    AppDialog(
        title = stringResource(R.string.package_inventory_unavailable_title),
        onDismiss = {},
        confirmLabel = stringResource(R.string.close),
        onConfirm = onClose,
    ) {
        AppText(stringResource(R.string.package_inventory_unavailable_body))
    }
}
