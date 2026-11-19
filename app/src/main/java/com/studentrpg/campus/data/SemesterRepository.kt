package com.studentrpg.campus.data

import campus.CampusState
import java.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SemesterRepository(private val dao: SemesterDao) {
    suspend fun load(): CampusState? = withContext(Dispatchers.IO) {
        dao.load()?.let { row ->
            ObjectInputStream(ByteArrayInputStream(row.snapshot)).use { it.readObject() as CampusState }
        }
    }
    suspend fun save(state: CampusState) = withContext(Dispatchers.IO) {
        val bytes = ByteArrayOutputStream().use { buffer ->
            ObjectOutputStream(buffer).use { it.writeObject(state) }
            buffer.toByteArray()
        }
        dao.save(SemesterEntity(snapshot = bytes))
    }
}
