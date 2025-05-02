package com.synaptix.budgetbuddy.data.entity.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.synaptix.budgetbuddy.data.entity.BudgetEntity
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.LabelEntity
import com.synaptix.budgetbuddy.data.entity.TransactionEntity
import com.synaptix.budgetbuddy.data.entity.TransactionLabelEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import com.synaptix.budgetbuddy.data.entity.WalletEntity

data class CategoryWithUser(
    @Embedded val category: CategoryEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val user: UserEntity?
)

data class WalletWithUser(
    @Embedded val wallet: WalletEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val user: UserEntity?
)

data class BudgetWithUser(
    @Embedded val budget: BudgetEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val user: UserEntity?
)

/// Relation for Transaction with label due to many to many relationship
// AI assisted with the creation of this data class
data class TransactionWithLabels(
    @Embedded val transaction: TransactionEntity,

    @Relation(
        parentColumn = "transaction_id",
        entityColumn = "label_id",
        associateBy = Junction(TransactionLabelEntity::class)
    )
    val labels: List<LabelEntity>
)