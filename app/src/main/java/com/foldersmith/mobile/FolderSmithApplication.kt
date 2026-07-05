package com.foldersmith.mobile

import android.app.Application
import com.foldersmith.mobile.data.FolderSmithDatabase
import com.foldersmith.mobile.data.FolderSmithRepository
import com.foldersmith.mobile.organize.SafeFileOrganizer
import com.foldersmith.mobile.scanner.AndroidContentScanner
import com.foldersmith.mobile.scanner.FileHasher

class FolderSmithApplication : Application() {
    val database: FolderSmithDatabase by lazy { FolderSmithDatabase.create(this) }
    val repository: FolderSmithRepository by lazy {
        FolderSmithRepository(
            database = database,
            organizer = SafeFileOrganizer(this, contentResolver),
            scanner = AndroidContentScanner(
                context = this,
                contentResolver = contentResolver,
                fileHasher = FileHasher(contentResolver)
            )
        )
    }
}
