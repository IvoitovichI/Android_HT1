package com.studentrpg.campus

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import campus.CampusGame
import com.studentrpg.campus.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlanRepositoryTest {
    private lateinit var db: CampusDatabase
    private lateinit var repository: PlanRepository
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "plan-test.db"
    private fun open() {
        db = Room.databaseBuilder(context, CampusDatabase::class.java, name).build()
        repository = PlanRepository(db)
    }
    @Before fun prepare() { context.deleteDatabase(name); open() }
    @After fun cleanup() { db.close(); context.deleteDatabase(name) }
    private suspend fun plan() = repository.plans.first().single()
    private suspend fun rejected(action: suspend () -> Unit) {
        try { action(); fail("Expected validation failure") } catch (_: IllegalArgumentException) { }
    }

    @Test fun crudPersistsAfterClosingDatabaseAndCascadesHistory() = runBlocking {
        repository.save(null, "Kotlin", "Prepare for exam", 3)
        val id = plan().id
        repository.save(id, "Kotlin exam", "Read notes", 4)
        db.close(); open()
        assertEquals("Kotlin exam", plan().title)
        assertEquals(4, plan().targetSteps)
        assertEquals(2, repository.history.first().size)
        repository.delete(id)
        assertTrue(repository.plans.first().isEmpty())
        assertTrue(repository.history.first().isEmpty())
    }

    @Test fun workflowConditionsUndoReopenAndHistoryPersist() = runBlocking {
        repository.save(null, "Exam plan", "", 2)
        val id = plan().id
        rejected { repository.act(id, PlanAction.STEP) }
        repository.act(id, PlanAction.START)
        rejected { repository.act(id, PlanAction.COMPLETE) }
        repository.act(id, PlanAction.STEP)
        repository.act(id, PlanAction.UNDO_STEP)
        assertEquals(0, plan().completedSteps)
        repository.act(id, PlanAction.STEP)
        repository.act(id, PlanAction.STEP)
        rejected { repository.act(id, PlanAction.STEP) }
        repository.act(id, PlanAction.COMPLETE)
        rejected { repository.save(id, "Changed", "", 2) }
        rejected { repository.act(id, PlanAction.UNDO_STEP) }
        db.close(); open()
        assertEquals(StudyPlan.DONE, plan().status)
        assertEquals(7, repository.history.first().size)
        repository.act(id, PlanAction.REOPEN)
        repository.act(id, PlanAction.UNDO_STEP)
        assertEquals(StudyPlan.ACTIVE, plan().status)
        assertEquals(1, plan().completedSteps)
    }

    @Test fun invalidEditsDoNotChangeDataOrHistory() = runBlocking {
        rejected { repository.save(null, "  ", "", 2) }
        rejected { repository.save(null, "Title", "", 101) }
        rejected { repository.save(null, "Title", "x".repeat(1001), 2) }
        assertTrue(repository.plans.first().isEmpty())
        repository.save(null, "Valid title", "", 3)
        val id = plan().id
        repository.act(id, PlanAction.START)
        repeat(2) { repository.act(id, PlanAction.STEP) }
        val events = repository.history.first().size
        rejected { repository.save(id, "Updated title", "", 1) }
        assertEquals("Valid title", plan().title)
        assertEquals(events, repository.history.first().size)
    }

    @Test fun roomFlowEmitsChangesAndConcurrentStepsStayConsistent() = runBlocking {
        val observed = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeout(10000) { repository.plans.first { it.isNotEmpty() } }
        }
        repository.save(null, "Concurrent plan", "", 10)
        val id = observed.await().single().id
        repository.act(id, PlanAction.START)
        coroutineScope { (1..10).map { async(Dispatchers.IO) { repository.act(id, PlanAction.STEP) } }.awaitAll() }
        assertEquals(10, plan().completedSteps)
        assertEquals(12, repository.history.first().size)
    }

    @Test fun semesterSnapshotPersistsInventoryQuizAndProgress() = runBlocking {
        val game = CampusGame()
        game.buy("coffee")
        game.startLesson(0)
        val before = game.state
        assertNotNull(before.quiz)
        SemesterRepository(db.semester()).save(before)
        db.close(); open()
        assertEquals(before, SemesterRepository(db.semester()).load())
    }

    @Test fun formRejectsBlankOverflowAndTargetsBelowProgress() {
        assertNotNull(PlanForm(title = "  ").titleError)
        assertNotNull(PlanForm(title = "a".repeat(81)).titleError)
        listOf("", "abc", "1.5", "0", "101", "999999999999999").forEach {
            assertNotNull(PlanForm(target = it).targetError)
        }
        assertNotNull(PlanForm(target = "2", minimum = 3).targetError)
        assertNull(PlanForm(title = "Exam", target = "3", minimum = 3).targetError)
    }
}
