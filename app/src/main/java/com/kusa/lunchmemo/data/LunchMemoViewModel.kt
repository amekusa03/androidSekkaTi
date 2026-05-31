package com.kusa.sekkati.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SekkaTiViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = SekkaTiDatabase.getDatabase(application).sekkaTiDao()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    val memos = dao.getAllMemos().map { list ->
        list.associate { LocalDate.parse(it.date, formatter) to it.memo }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun saveMemo(date: LocalDate, memo: String) {
        viewModelScope.launch {
            dao.insertMemo(SekkaTiEntity(date.format(formatter), memo))
        }
    }

    fun cleanOldMemos() {
        viewModelScope.launch {
            // 自動クリーンアップは1ヶ月以上前に変更（要件に合わせて調整）
            val monthAgo = LocalDate.now().minusMonths(1).format(formatter)
            dao.deleteMemosOlderThan(monthAgo)
        }
    }

    fun deleteMemo(date: LocalDate) {
        viewModelScope.launch {
            dao.deleteMemoByDate(date.format(formatter))
        }
    }

    fun deleteRange(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            dao.deleteMemosInRange(startDate.format(formatter), endDate.format(formatter))
        }
    }

    fun deleteYesterday() {
        deleteMemo(LocalDate.now().minusDays(1))
    }

    fun deleteLastWeek() {
        val end = LocalDate.now().minusDays(1)
        val start = end.minusWeeks(1)
        deleteRange(start, end)
    }

    fun deleteLastMonth() {
        val end = LocalDate.now().minusDays(1)
        val start = end.minusMonths(1)
        deleteRange(start, end)
    }
}
