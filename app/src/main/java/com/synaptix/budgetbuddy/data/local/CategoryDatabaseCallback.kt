//

package com.synaptix.budgetbuddy.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.synaptix.budgetbuddy.data.AppDatabase
import com.synaptix.budgetbuddy.data._default.CategoryDefualts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ===================================
// CategoryDatabaseCallback
// ===================================
// This class populates default categories into the database when it is first created.
// It fetches values from the CategoryDefaults object.
class CategoryDatabaseCallback(
    private val dbProvider: () -> AppDatabase
) : RoomDatabase.Callback() {

    // ===================================
    // onCreate - Populates Default Categories
    // ===================================
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val database = dbProvider()
            val dao = database.categoryDao()
            CategoryDefualts.defaultCategories.forEach { dao.insertCategory(it) }
        }
    }
}
