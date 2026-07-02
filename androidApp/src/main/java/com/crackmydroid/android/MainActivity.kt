package com.crackmydroid.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.crackmydroid.shared.ui.CrackMyDroidApp
import com.crackmydroid.shared.presentation.operations.init

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Init operation log persistence
        com.crackmydroid.shared.presentation.operations.OperationLogStore.init(applicationContext)
        setContent {
            Surface {
                CrackMyDroidApp()
            }
        }
    }
}
