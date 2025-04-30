package com.synaptix.budgetbuddy.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.synaptix.budgetbuddy.data.AppDatabase
import com.synaptix.budgetbuddy.data._default.CategoryDefualts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


//AI assisted with the creation of this class that allows for defualt categories to be loaded onto the database when first created
//class calls for values from the CategoryDefaults object
class CategoryDatabaseCallback (
    private val dbProvider: () -> AppDatabase
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val database = dbProvider()
            val dao = database.categoryDao()
            CategoryDefualts.defaultCategories.forEach { dao.insertCategory(it) }
        }
    }
}