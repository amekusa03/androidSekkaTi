package com.kusa.sekkati.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lunch_memos")
data class SekkaTiEntity(
    @PrimaryKey val date: String, // yyyy-MM-dd
    val memo: String
)
