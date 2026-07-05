package com.foldersmith.mobile.domain

import com.foldersmith.mobile.data.CleanupActionEntity
import com.foldersmith.mobile.model.CleanupActionType
import com.foldersmith.mobile.model.CleanupStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class UndoSessionGeneratorTest {
    @Test
    fun includesOnlyAppliedMoveLikeActionsWithDestinations() {
        val actions = listOf(
            action(1, CleanupActionType.Move, CleanupStatus.Applied, "content://new/1"),
            action(2, CleanupActionType.Archive, CleanupStatus.Applied, "content://archive/2"),
            action(3, CleanupActionType.Delete, CleanupStatus.Applied, null),
            action(4, CleanupActionType.Move, CleanupStatus.Planned, "content://new/4")
        )

        val undoable = UndoSessionGenerator.undoableActions(actions)

        assertEquals(listOf(1L, 2L), undoable.map { it.id })
    }

    private fun action(
        id: Long,
        type: CleanupActionType,
        status: CleanupStatus,
        destination: String?
    ) = CleanupActionEntity(
        id = id,
        fileId = id,
        actionType = type,
        sourceUri = "content://old/$id",
        destinationUri = destination,
        status = status,
        sessionId = 1
    )
}
