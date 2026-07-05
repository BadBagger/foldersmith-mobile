package com.foldersmith.mobile.domain

import com.foldersmith.mobile.data.CleanupActionEntity
import com.foldersmith.mobile.model.CleanupActionType
import com.foldersmith.mobile.model.CleanupStatus

object UndoSessionGenerator {
    fun undoableActions(actions: List<CleanupActionEntity>): List<CleanupActionEntity> {
        return actions.filter {
            it.status == CleanupStatus.Applied &&
                it.destinationUri != null &&
                it.actionType in setOf(CleanupActionType.Move, CleanupActionType.Archive)
        }
    }
}
