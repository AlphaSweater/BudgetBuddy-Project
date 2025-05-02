package com.synaptix.budgetbuddy.data.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
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