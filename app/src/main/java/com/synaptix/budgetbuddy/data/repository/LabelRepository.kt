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

package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.data.entity.LabelEntity
import com.synaptix.budgetbuddy.data.local.dao.LabelDao

// ===================================
// LabelRepository
// ===================================
// This repository handles operations related to labels,
// such as fetching user-specific labels from the database.
class LabelRepository(private val labelDao: LabelDao) {

    // ===================================
    // getLabelsForUser - Fetch Labels for User
    // ===================================
    suspend fun getLabelsForUser(userId: Int): List<LabelEntity> {
        return labelDao.getLabelsForUser(userId)
    }
}
