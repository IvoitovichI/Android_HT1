package com.studentrpg.campus

import androidx.lifecycle.SavedStateHandle
import campus.CampusState
import campus.QuizProgress
import com.studentrpg.campus.data.SemesterSavedState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SemesterSavedStateTest {
    @Test fun missingSaveReturnsNull() {
        assertNull(SemesterSavedState(SavedStateHandle()).restore())
    }

    @Test fun corruptSaveReturnsNull() {
        val handle = SavedStateHandle(mapOf("semester" to byteArrayOf(1, 2, 3)))
        assertNull(SemesterSavedState(handle).restore())
    }

    @Test fun activeQuizAndAllResourcesSurviveNewAdapter() {
        val expected = CampusState(
            health = 45, mana = 30, mind = 51, day = 5, minutesLeft = 120,
            coins = 81, xp = 135, passes = mapOf("kotlin" to 1),
            knowledge = mapOf("kotlin" to 16), inventory = mapOf("tea" to 2),
            quiz = QuizProgress(0, 1, 2, 1, highlighted = true, eliminated = true, guaranteed = true),
            message = "Saved quiz", journal = listOf("Day 5")
        )
        val original = SavedStateHandle()
        SemesterSavedState(original).save(expected)
        val bytes = original.get<ByteArray>("semester")
        assertNotNull(bytes)
        val recreated = SavedStateHandle(mapOf("semester" to checkNotNull(bytes)))
        assertEquals(expected, SemesterSavedState(recreated).restore())
    }
}
