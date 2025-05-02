package com.synaptix.budgetbuddy.data.repository

import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import com.synaptix.budgetbuddy.data.entity.mapper.toDomain
import com.synaptix.budgetbuddy.data.local.dao.WalletDao
import javax.inject.Inject

class WalletRepository @Inject constructor(private val walletDao: WalletDao) {
    suspend fun getWalletsByUserId(userId: Int): List<Wallet> {
        val wallets = walletDao.getWalletsByUserId(userId)
        return wallets.map { it.toDomain() }
    }
}