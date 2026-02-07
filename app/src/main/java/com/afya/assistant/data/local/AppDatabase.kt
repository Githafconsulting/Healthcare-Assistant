package com.afya.assistant.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Single database file with all entities and DAOs.
 * Offline-first: patients, visits, voice notes, follow-ups, audit.
 * Security: no PHI in audit details; entities in app-private storage.
 */
@Database(
    entities = [
        PatientEntity::class,
        VisitEntity::class,
        AuditEntity::class,
        VoiceNoteEntity::class,
        FollowUpEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun visitDao(): VisitDao
    abstract fun auditDao(): AuditDao
    abstract fun voiceNoteDao(): VoiceNoteDao
    abstract fun followUpDao(): FollowUpDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "afya.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS voice_notes (
                        id TEXT PRIMARY KEY NOT NULL,
                        visitId TEXT NOT NULL,
                        filePath TEXT,
                        durationMs INTEGER,
                        transcript TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        synced INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS follow_ups (
                        id TEXT PRIMARY KEY NOT NULL,
                        visitId TEXT NOT NULL,
                        patientId TEXT NOT NULL,
                        dueDateEpoch INTEGER NOT NULL,
                        reason TEXT NOT NULL,
                        reminderSentAt INTEGER,
                        smsConsent INTEGER NOT NULL DEFAULT 0,
                        synced INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
    }
}

// ==================== ENTITIES ====================

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dateOfBirth: Long,  // epoch days
    val sex: String,
    val village: String,
    val phone: String?,
    val caregiverName: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val synced: Boolean = false
)

@Entity(
    tableName = "visits",
    foreignKeys = [ForeignKey(
        entity = PatientEntity::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientId")]
)
data class VisitEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val chwId: String,
    val startTime: Long,
    val endTime: Long?,
    val symptomsJson: String,      // JSON array
    val vitalsJson: String?,       // JSON object
    val dangerSignsJson: String,   // JSON array of strings
    val assessment: String?,
    val treatment: String?,
    val referralJson: String?,     // JSON object
    val notes: String?,
    val synced: Boolean = false
)

@Entity(tableName = "audit")
data class AuditEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val chwId: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val details: String?,  // No PHI: use IDs only
    val synced: Boolean = false
)

/** Voice note linked to a visit; optional file path for audio reference. */
@Entity(
    tableName = "voice_notes",
    foreignKeys = [ForeignKey(
        entity = VisitEntity::class,
        parentColumns = ["id"],
        childColumns = ["visitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("visitId")]
)
data class VoiceNoteEntity(
    @PrimaryKey val id: String,
    val visitId: String,
    val filePath: String?,
    val durationMs: Int?,
    val transcript: String,
    val createdAt: Long,
    val synced: Boolean = false
)

/** Follow-up reminder for SMS; consent required before send. */
@Entity(
    tableName = "follow_ups",
    indices = [Index("visitId"), Index("dueDateEpoch")]
)
data class FollowUpEntity(
    @PrimaryKey val id: String,
    val visitId: String,
    val patientId: String,
    val dueDateEpoch: Long,
    val reason: String,
    val reminderSentAt: Long?,
    val smsConsent: Boolean,
    val synced: Boolean = false
)

// ==================== DAOs ====================

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY name")
    fun getAll(): Flow<List<PatientEntity>>
    
    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getById(id: String): PatientEntity?
    
    @Query("SELECT * FROM patients WHERE name LIKE '%' || :query || '%' LIMIT 20")
    suspend fun search(query: String): List<PatientEntity>
    
    @Query("SELECT * FROM patients WHERE synced = 0")
    suspend fun getUnsynced(): List<PatientEntity>
    
    @Query("SELECT COUNT(*) FROM patients WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
    
    @Query("UPDATE patients SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: PatientEntity)
    
    @Update
    suspend fun update(patient: PatientEntity)
    
    @Query("DELETE FROM patients WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface VisitDao {
    @Query("SELECT * FROM visits WHERE patientId = :patientId ORDER BY startTime DESC")
    fun getByPatient(patientId: String): Flow<List<VisitEntity>>
    
    @Query("SELECT * FROM visits WHERE id = :id")
    suspend fun getById(id: String): VisitEntity?
    
    @Query("SELECT * FROM visits WHERE synced = 0")
    suspend fun getUnsynced(): List<VisitEntity>
    
    @Query("SELECT COUNT(*) FROM visits WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
    
    @Query("UPDATE visits SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(visit: VisitEntity)
    
    @Update
    suspend fun update(visit: VisitEntity)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit WHERE synced = 0 LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<AuditEntity>

    @Query("SELECT COUNT(*) FROM audit WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("UPDATE audit SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSyncedBatch(ids: List<String>)

    @Insert
    suspend fun insert(entry: AuditEntity)
}

@Dao
interface VoiceNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voiceNote: VoiceNoteEntity)

    @Query("SELECT * FROM voice_notes WHERE visitId = :visitId LIMIT 1")
    suspend fun getByVisit(visitId: String): VoiceNoteEntity?

    @Query("SELECT * FROM voice_notes WHERE synced = 0")
    suspend fun getUnsynced(): List<VoiceNoteEntity>
}

@Dao
interface FollowUpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(followUp: FollowUpEntity)

    @Query("SELECT * FROM follow_ups WHERE dueDateEpoch <= :epoch AND reminderSentAt IS NULL AND smsConsent = 1")
    suspend fun getDueForReminder(epoch: Long): List<FollowUpEntity>

    @Query("UPDATE follow_ups SET reminderSentAt = :at WHERE id = :id")
    suspend fun markReminderSent(id: String, at: Long)

    @Query("SELECT * FROM follow_ups WHERE patientId = :patientId ORDER BY dueDateEpoch DESC")
    fun getByPatient(patientId: String): Flow<List<FollowUpEntity>>
}
