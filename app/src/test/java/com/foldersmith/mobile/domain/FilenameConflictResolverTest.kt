package com.foldersmith.mobile.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FilenameConflictResolverTest {
    @Test
    fun keepsNameWhenNoConflictExists() {
        assertEquals("receipt.pdf", FilenameConflictResolver.safeName("receipt.pdf", setOf("other.pdf")))
    }

    @Test
    fun addsCounterBeforeExtensionWhenNameExists() {
        assertEquals(
            "receipt (2).pdf",
            FilenameConflictResolver.safeName("receipt.pdf", setOf("receipt.pdf", "receipt (1).pdf"))
        )
    }
}
