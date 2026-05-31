package com.kusa.sekkati.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SekkaTiEntity::class], version = 1)
abstract class SekkaTiDatabase : RoomDatabase() {
    abstract fun sekkaTiDao(): SekkaTiDao

    companion object {
        @Volatile
        private var INSTANCE: SekkaTiDatabase? = null

        fun getDatabase(context: Context): SekkaTiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SekkaTiDatabase::class.java,
                    "lunch_memo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
