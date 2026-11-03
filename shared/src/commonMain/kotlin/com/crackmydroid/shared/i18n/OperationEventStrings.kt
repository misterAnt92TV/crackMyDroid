package com.crackmydroid.shared.i18n

import com.crackmydroid.shared.domain.model.OperationEvent
import com.crackmydroid.shared.domain.model.OperationEventKey

fun Strings.renderOperation(event: OperationEvent): String = when (event.key) {
    OperationEventKey.ACTIVITY_EXPORT -> exportLog
    OperationEventKey.ACTIVITY_LOAD -> navActivities
    OperationEventKey.APPS_EXPORT -> apkExported
    OperationEventKey.APPS_SHARE -> apkShared
    OperationEventKey.PERMISSIONS_EXPORT, OperationEventKey.LOGS_EXPORT -> exportLog
    OperationEventKey.PENTEST_EXPORT -> pentestExport
    else -> event.key
}
