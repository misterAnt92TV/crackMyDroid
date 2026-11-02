package com.crackmydroid.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.crackmydroid.shared.ui.CrackMyDroidApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageInventoryAccess = packageInventoryAccess(
            sdkInt = android.os.Build.VERSION.SDK_INT,
            queryAllPackagesPermissionGranted = checkSelfPermission(
                Manifest.permission.QUERY_ALL_PACKAGES
            ) == PackageManager.PERMISSION_GRANTED
        )
        val disclosurePreferences = getSharedPreferences(DISCLOSURE_PREFERENCES, Context.MODE_PRIVATE)
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
        title = { Text("Scansione app installate") },
        text = {
            Text(
                "CrackMyDroid legge localmente l'inventario delle app installate per mostrare APK, " +
                    "activity, componenti e permessi. Non viene mostrato un dialogo di sistema: " +
                    "l'accesso alla visibilita dei package e dichiarato nel manifest. " +
                    "L'esportazione o la condivisione di un APK avviene solo quando la avvii tu."
            )
        },
        confirmButton = {
            Button(onClick = onAcknowledge) {
                Text("Ho capito")
            }
        }
    )
}

@androidx.compose.runtime.Composable
private fun PackageInventoryUnavailableDialog(onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Scansione completa non disponibile") },
        text = {
            Text(
                "Questa installazione non dispone della visibilita completa delle app. " +
                    "Per evitare risultati parziali, la scansione non viene avviata. Reinstalla un APK " +
                    "che dichiari QUERY_ALL_PACKAGES nel manifest."
            )
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text("Chiudi")
            }
        }
    )
}
