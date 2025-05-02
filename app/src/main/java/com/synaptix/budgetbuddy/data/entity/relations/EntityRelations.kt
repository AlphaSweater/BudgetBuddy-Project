package com.synaptix.budgetbuddy.data.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity

data class CategoryWithUser(
    @Embedded val category: CategoryEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val user: UserEntity?
)
