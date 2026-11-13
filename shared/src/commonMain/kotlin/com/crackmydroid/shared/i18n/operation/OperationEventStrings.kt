package com.crackmydroid.shared.i18n.operation

import com.crackmydroid.shared.domain.model.operation.OperationEvent
import com.crackmydroid.shared.domain.model.operation.OperationEventKey
import com.crackmydroid.shared.i18n.Strings

fun Strings.renderOperation(event: OperationEvent): String = when (event.key) {
    OperationEventKey.ACTIVITY_EXPORT -> exportLog
    OperationEventKey.ACTIVITY_LOAD -> navActivities
    OperationEventKey.APPS_EXPORT -> apkExported
    OperationEventKey.APPS_SHARE -> apkShared
    OperationEventKey.PERMISSIONS_EXPORT, OperationEventKey.LOGS_EXPORT -> exportLog
    OperationEventKey.PENTEST_EXPORT -> pentestExport
    else -> event.key
}
