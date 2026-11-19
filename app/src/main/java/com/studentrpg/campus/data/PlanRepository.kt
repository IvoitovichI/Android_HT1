package com.studentrpg.campus.data

import androidx.room.withTransaction

class PlanRepository(private val db: CampusDatabase) {
    private val dao = db.plans()
    val plans = dao.observePlans()
    val history = dao.observeHistory()

    suspend fun save(id: Long?, title: String, description: String, target: Int) = db.withTransaction {
        require(title.trim().length in 3..80) { "Название: от 3 до 80 символов." }
        require(description.length <= 1000) { "Описание: максимум 1000 символов." }
        require(target in 1..100) { "Количество шагов: от 1 до 100." }
        if (id == null) {
            val newId = dao.insert(StudyPlan(title = title.trim(), description = description.trim(), targetSteps = target))
            dao.record(PlanEvent(planId = newId, text = "План создан: $target шагов"))
        } else {
            val old = requireNotNull(dao.find(id)) { "План уже удалён." }
            require(old.status != StudyPlan.DONE) { "Сначала верните план в работу." }
            require(target >= old.completedSteps) { "Цель не может быть меньше выполненных шагов (${old.completedSteps})." }
            dao.update(old.copy(title = title.trim(), description = description.trim(), targetSteps = target))
            dao.record(PlanEvent(planId = id, text = "План отредактирован: цель $target шагов"))
        }
    }

    suspend fun act(id: Long, action: PlanAction) = db.withTransaction {
        val old = requireNotNull(dao.find(id)) { "План уже удалён." }
        val (next, message) = when (action) {
            PlanAction.START -> {
                require(old.status == StudyPlan.DRAFT) { "Начать можно только черновик." }
                old.copy(status = StudyPlan.ACTIVE) to "План начат"
            }
            PlanAction.STEP -> {
                require(old.status == StudyPlan.ACTIVE && old.completedSteps < old.targetSteps) { "Нет доступного шага." }
                old.copy(completedSteps = old.completedSteps + 1) to "Выполнен шаг ${old.completedSteps + 1}/${old.targetSteps}"
            }
            PlanAction.UNDO_STEP -> {
                require(old.status == StudyPlan.ACTIVE && old.completedSteps > 0) { "Нечего отменять." }
                old.copy(completedSteps = old.completedSteps - 1) to "Последний шаг отменён"
            }
            PlanAction.COMPLETE -> {
                require(old.status == StudyPlan.ACTIVE && old.completedSteps == old.targetSteps) { "Сначала выполните все шаги." }
                old.copy(status = StudyPlan.DONE) to "План завершён"
            }
            PlanAction.REOPEN -> {
                require(old.status == StudyPlan.DONE) { "Вернуть можно только завершённый план." }
                old.copy(status = StudyPlan.ACTIVE) to "План возвращён в работу"
            }
        }
        dao.update(next)
        dao.record(PlanEvent(planId = id, text = message))
    }

    suspend fun delete(id: Long) = db.withTransaction {
        require(dao.delete(id) == 1) { "План уже удалён." }
    }
}

enum class PlanAction { START, STEP, UNDO_STEP, COMPLETE, REOPEN }
