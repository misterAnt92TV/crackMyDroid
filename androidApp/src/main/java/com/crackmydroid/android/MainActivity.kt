package com.crackmydroid.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.crackmydroid.shared.ui.CrackMyDroidApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val packageInventoryAccess = packageInventoryAccess(
            sdkInt = android.os.Build.VERSION.SDK_INT,
            queryAllPackagesPermissionGranted = checkSelfPermission(
                Manifest.permission.QUERY_ALL_PACKAGES
            ) == PackageManager.PERMISSION_GRANTED
        )
        val disclosurePreferences =
            getSharedPreferences(DISCLOSURE_PREFERENCES, Context.MODE_PRIVATE)
        val disclosureSeen = disclosurePreferences.getBoolean(DISCLOSURE_SEEN_KEY, false)
        setContent {
            Surface {
                when (packageInventoryAccess) {
                    PackageInventoryAccess.Full -> {
                        CrackMyDroidApp()
                        var showDisclosure by remember { mutableStateOf(!disclosureSeen) }
                        if (showDisclosure) {
                            PackageInventoryDisclosure(
                                onAcknowledge = {
                                    disclosurePreferences.edit()
                                        .putBoolean(DISCLOSURE_SEEN_KEY, true)
                                        .apply()
                                    showDisclosure = false
                                }
                            )
                        }
                    }

                    PackageInventoryAccess.Unavailable -> PackageInventoryUnavailableDialog(
                        onClose = ::finish
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
    AlertDialog(
        onDismissRequest = onAcknowledge,
        title = { Text(stringResource(R.string.package_inventory_disclosure_title)) },
        text = {
            Text(
                stringResource(R.string.package_inventory_disclosure_body)
            )
        },
        confirmButton = {
            Button(onClick = onAcknowledge) {
                Text(stringResource(R.string.package_inventory_disclosure_confirm))
            }
        }
    )
}

@androidx.compose.runtime.Composable
private fun PackageInventoryUnavailableDialog(onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.package_inventory_unavailable_title)) },
        text = {
            Text(
                stringResource(R.string.package_inventory_unavailable_body)
            )
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
