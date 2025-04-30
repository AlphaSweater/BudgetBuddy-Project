package com.synaptix.budgetbuddy.core.usecase.main.wallet

import com.synaptix.budgetbuddy.data.entity.WalletEntity
import com.synaptix.budgetbuddy.data.local.dao.WalletDao
import javax.inject.Inject

class AddWalletUseCase @Inject constructor(
    private val walletDao: WalletDao
) {
    suspend fun execute(wallet: WalletEntity): Long {
        //println for logcat
        println("wallet to be inserted: $wallet")

        return walletDao.insertWallet(wallet)
    }
}