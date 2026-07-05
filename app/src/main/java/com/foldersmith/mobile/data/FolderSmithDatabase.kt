package com.foldersmith.mobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.foldersmith.mobile.model.EnumConverters

@Database(
    entities = [
        ScannedFileEntity::class,
        DuplicateGroupEntity::class,
        CleanupActionEntity::class,
        CleanupSessionEntity::class,
        PhotoEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(EnumConverters::class)
abstract class FolderSmithDatabase : RoomDatabase() {
    abstract fun dao(): FolderSmithDao

    companion object {
        fun create(context: Context): FolderSmithDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FolderSmithDatabase::class.java,
                "foldersmith_mobile.db"
            ).build()
        }
    }
}
