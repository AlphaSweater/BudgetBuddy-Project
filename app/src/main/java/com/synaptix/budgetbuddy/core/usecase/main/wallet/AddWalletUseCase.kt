package com.synaptix.budgetbuddy.core.usecase.main.wallet

import com.synaptix.budgetbuddy.core.model.Wallet

import com.synaptix.budgetbuddy.data.local.dao.WalletDao
import com.synaptix.budgetbuddy.data.local.mapper.toEntity

import javax.inject.Inject

class AddWalletUseCase @Inject constructor(
    private val walletDao: WalletDao
) {
    suspend fun execute(wallet: Wallet): Long {
        //println for logcat
        println("wallet to be inserted: $wallet")

        return walletDao.insertWallet(wallet.toEntity())
    }
}