package com.synaptix.budgetbuddy.core.usecase.main.wallet

import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.data.entity.WalletEntity
import com.synaptix.budgetbuddy.data.repository.WalletRepository
import javax.inject.Inject


class GetWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend fun execute(userId: Int): List<Wallet> {
        return walletRepository.getWalletsByUserId(userId)
    }
}