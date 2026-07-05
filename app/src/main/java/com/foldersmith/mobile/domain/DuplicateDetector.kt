package com.foldersmith.mobile.domain

import com.foldersmith.mobile.data.DuplicateGroupEntity
import com.foldersmith.mobile.data.ScannedFileEntity

object DuplicateDetector {
    fun exactDuplicateGroups(files: List<ScannedFileEntity>): List<DuplicateGroupEntity> {
        return files
            .filter { it.hash != null && it.sizeBytes > 0L }
            .groupBy { it.sizeBytes }
            .values
            .filter { it.size > 1 }
            .flatMap { sameSize -> sameSize.groupBy { it.hash.orEmpty() }.values }
            .filter { it.size > 1 }
            .map { duplicates ->
                val recommended = duplicates.maxWithOrNull(
                    compareBy<ScannedFileEntity> { it.dateTaken ?: 0L }
                        .thenBy { it.dateModified ?: 0L }
                        .thenBy { it.displayName.length * -1 }
                )
                DuplicateGroupEntity(
                    hash = duplicates.first().hash.orEmpty(),
                    sizeBytes = duplicates.first().sizeBytes,
                    fileIds = duplicates.joinToString(",") { it.id.toString() },
                    recommendedKeepFileId = recommended?.id
                )
            }
    }

    fun candidateFilesForHashing(files: List<ScannedFileEntity>, maxImmediateHashBytes: Long): List<ScannedFileEntity> {
        return files
            .groupBy { it.sizeBytes }
            .values
            .filter { it.size > 1 }
            .flatten()
            .filter { it.sizeBytes <= maxImmediateHashBytes }
    }
}
