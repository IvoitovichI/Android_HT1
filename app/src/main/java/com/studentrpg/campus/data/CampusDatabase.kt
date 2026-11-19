package com.studentrpg.campus.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "study_plans")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val targetSteps: Int,
    val completedSteps: Int = 0,
    val status: String = DRAFT
) {
    companion object {
        const val DRAFT = "DRAFT"
        const val ACTIVE = "ACTIVE"
        const val DONE = "DONE"
    }
}

@Entity(tableName = "plan_history", foreignKeys = [ForeignKey(
    entity = StudyPlan::class, parentColumns = ["id"], childColumns = ["planId"],
    onDelete = ForeignKey.CASCADE
)], indices = [Index("planId")])
data class PlanEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "semester_save")
data class SemesterEntity(@PrimaryKey val id: Int = 1, val snapshot: ByteArray)

@Dao
interface PlanDao {
    @Query("SELECT * FROM study_plans ORDER BY id DESC")
    fun observePlans(): Flow<List<StudyPlan>>
    @Query("SELECT * FROM plan_history ORDER BY id DESC")
    fun observeHistory(): Flow<List<PlanEvent>>
    @Query("SELECT * FROM study_plans WHERE id = :id")
    suspend fun find(id: Long): StudyPlan?
    @Insert suspend fun insert(plan: StudyPlan): Long
    @Update suspend fun update(plan: StudyPlan)
    @Query("DELETE FROM study_plans WHERE id = :id") suspend fun delete(id: Long): Int
    @Insert suspend fun record(event: PlanEvent)
}

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semester_save WHERE id = 1") suspend fun load(): SemesterEntity?
    @Upsert suspend fun save(value: SemesterEntity)
}

@Database(entities = [StudyPlan::class, PlanEvent::class, SemesterEntity::class], version = 1, exportSchema = true)
abstract class CampusDatabase : RoomDatabase() {
    abstract fun plans(): PlanDao
    abstract fun semester(): SemesterDao

    companion object {
        @Volatile private var instance: CampusDatabase? = null
        fun get(context: Context): CampusDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, CampusDatabase::class.java, "campus.db")
                .build().also { instance = it }
        }
    }
}
