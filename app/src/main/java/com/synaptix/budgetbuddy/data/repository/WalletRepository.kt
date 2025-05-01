package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.data.entity.WalletEntity
import com.synaptix.budgetbuddy.data.local.dao.WalletDao
import javax.inject.Inject

class WalletRepository @Inject constructor(private val walletDao: WalletDao) {
    suspend fun getWalletsByUserId(userId: Int): List<WalletEntity> {
        return walletDao.getWalletsByUserId(userId)
    }
}