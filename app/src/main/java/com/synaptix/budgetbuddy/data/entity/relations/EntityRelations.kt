// ======================================================================================
// Group 2 - Group Members:
// ======================================================================================
// * Chad Fairlie ST10269509
// * Dhiren Ruthenavelu ST10256859
// * Kayla Ferreira ST10259527
// * Nathan Teixeira ST10249266
// ======================================================================================
// Declaration:
// ======================================================================================
// We declare that this work is our own original work and that no part of it has been
// copied from any other source, except where explicitly acknowledged.
// ======================================================================================
// References:
// ======================================================================================
// * ChatGPT was used to help with the design and planning. As well as assisted with
//   finding and fixing errors in the code.
// * ChatGPT also helped with the forming of comments for the code.
// * https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
// ======================================================================================

package com.synaptix.budgetbuddy.data.entity.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.synaptix.budgetbuddy.data.entity.BudgetCategoryCrossRef
import com.synaptix.budgetbuddy.data.entity.BudgetEntity
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.LabelEntity
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.TransactionLabelEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity

//
// ================================
// Category with User Relation
// ================================
// Represents a Category along with its related User
data class CategoryWithUser(
    @Embedded val category: CategoryEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val user: UserEntity?
)

//
// ================================
// Wallet with User Relation
// ================================
// Represents a Wallet along with its related User
data class WalletWithUser(
    @Embedded val wallet: WalletEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val user: UserEntity?
)

data class BudgetWithDetail(
    @Embedded val budget: BudgetEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val user: UserEntity?,

    @Relation(
        parentColumn = "budget_id",
        entityColumn = "category_id",
        associateBy = Junction(
            value = BudgetCategoryCrossRef::class,
            parentColumn = "budget_id",
            entityColumn = "category_id"
        )
    )
    val categories: List<CategoryEntity>
)

//
// ================================
// Transaction with Detail Relation
// ================================
// Represents a Transaction along with its related User, Wallet, and Category
data class TransactionWithDetail(
    @Embedded val transaction: TransactionEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val user: UserEntity?,

    @Relation(
        parentColumn = "wallet_id",
        entityColumn = "wallet_id"
    )
    val wallet: WalletEntity?,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "category_id"
    )
    val category: CategoryEntity?
)