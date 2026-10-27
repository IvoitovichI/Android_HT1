package com.studentrpg.campus.data

import androidx.lifecycle.SavedStateHandle
import campus.CampusState
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/** Android saved-state adapter. Keeps the existing key and serialized format. */
internal class SemesterSavedState(private val handle: SavedStateHandle) {
    fun restore(): CampusState? = handle.get<ByteArray>(KEY)?.let { bytes ->
        runCatching {
            ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as CampusState }
        }.getOrNull()
    }

    fun save(state: CampusState) {
        handle[KEY] = ByteArrayOutputStream().use { buffer ->
            ObjectOutputStream(buffer).use { it.writeObject(state) }
            buffer.toByteArray()
        }
    }

    private companion object {
        const val KEY = "semester"
    }
}
