package com.foldersmith.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.foldersmith.mobile.model.CleanupStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderSmithDao {
    @Insert
    suspend fun insertSession(session: CleanupSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<ScannedFileEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuplicateGroups(groups: List<DuplicateGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCleanupActions(actions: List<CleanupActionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotoEvents(events: List<PhotoEventEntity>)

    @Update
    suspend fun updateCleanupAction(action: CleanupActionEntity)

    @Update
    suspend fun updateCleanupActions(actions: List<CleanupActionEntity>)

    @Query("SELECT * FROM scanned_files ORDER BY dateModified DESC")
    fun observeFiles(): Flow<List<ScannedFileEntity>>

    @Query("SELECT * FROM duplicate_groups ORDER BY sizeBytes DESC")
    fun observeDuplicateGroups(): Flow<List<DuplicateGroupEntity>>

    @Query("SELECT * FROM cleanup_actions ORDER BY id DESC")
    fun observeCleanupActions(): Flow<List<CleanupActionEntity>>

    @Query("SELECT * FROM cleanup_sessions ORDER BY createdAt DESC")
    fun observeCleanupSessions(): Flow<List<CleanupSessionEntity>>

    @Query("SELECT * FROM photo_events ORDER BY startDate DESC")
    fun observePhotoEvents(): Flow<List<PhotoEventEntity>>

    @Query("SELECT * FROM scanned_files WHERE category = :category ORDER BY dateModified DESC")
    fun observeFilesByCategory(category: com.foldersmith.mobile.model.FileCategory): Flow<List<ScannedFileEntity>>

    @Query("SELECT * FROM scanned_files WHERE id IN (:ids)")
    suspend fun filesByIds(ids: List<Long>): List<ScannedFileEntity>

    @Query("SELECT * FROM cleanup_actions WHERE sessionId = :sessionId")
    suspend fun cleanupActionsForSession(sessionId: Long): List<CleanupActionEntity>

    @Query("UPDATE cleanup_actions SET status = :status WHERE sessionId = :sessionId AND status = :fromStatus")
    suspend fun updateSessionActionStatus(sessionId: Long, fromStatus: CleanupStatus, status: CleanupStatus)

    @Query("UPDATE cleanup_sessions SET summary = :summary, canUndo = :canUndo WHERE id = :sessionId")
    suspend fun updateCleanupSessionSummary(sessionId: Long, summary: String, canUndo: Boolean)

    @Query("DELETE FROM scanned_files")
    suspend fun clearScannedFiles()

    @Query("DELETE FROM duplicate_groups")
    suspend fun clearDuplicateGroups()

    @Query("DELETE FROM photo_events")
    suspend fun clearPhotoEvents()
}
