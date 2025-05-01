package com.synaptix.budgetbuddy.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.synaptix.budgetbuddy.data.AppDatabase
import com.synaptix.budgetbuddy.data._default.LabelDefualts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LabelDatabaseCallback(
    private val dbProvider: () -> AppDatabase // Provides access to the database
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val database = dbProvider() // Get database instance
            val labelDao = database.labelDao() // Get the LabelDao

            // Insert default labels into the label table
            LabelDefualts.defaultLabels.forEach { labelDao.insert(it) }
        }
    }
}