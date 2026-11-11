package com.crackmydroid.shared.presentation.components

import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val iconCache = LruCache<String, ImageBitmap>(150)

@Composable
actual fun rememberPackageIcon(packageName: String): Painter? {
    val context = LocalContext.current.applicationContext
    val cached = iconCache.get(packageName)

    val imageBitmap = produceState<ImageBitmap?>(initialValue = cached, key1 = packageName) {
        if (cached != null) {
            value = cached
            return@produceState
        }
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                val drawable = context.packageManager.getApplicationIcon(packageName)
                drawable.toBitmap().asImageBitmap()
            }.getOrNull()
        }
        if (loaded != null) {
            iconCache.put(packageName, loaded)
            value = loaded
        }
    }

    return imageBitmap.value?.let { BitmapPainter(it) }
}
