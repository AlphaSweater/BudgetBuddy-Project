//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.synaptix.budgetbuddy.data.AppDatabase
import com.synaptix.budgetbuddy.data._default.LabelDefualts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ===================================
// LabelDatabaseCallback
// ===================================
// This class populates default labels into the database when it is first created.
// It fetches values from the LabelDefaults object.
class LabelDatabaseCallback(
    private val dbProvider: () -> AppDatabase // Provides access to the database
) : RoomDatabase.Callback() {

    // ===================================
    // onCreate - Populates Default Labels
    // ===================================
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
