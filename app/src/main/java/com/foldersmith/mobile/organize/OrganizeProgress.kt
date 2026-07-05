package com.foldersmith.mobile.organize

data class OrganizeProgress(
    val isApplying: Boolean = false,
    val completed: Int = 0,
    val total: Int = 0,
    val message: String = "No organizer running",
    val failed: Int = 0
)
