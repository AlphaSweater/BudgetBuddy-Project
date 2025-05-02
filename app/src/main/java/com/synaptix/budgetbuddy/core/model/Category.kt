package com.synaptix.budgetbuddy.core.model

import androidx.room.Embedded
import androidx.room.Relation
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import java.io.Serializable

data class Category (
    val categoryId: Int = 0,
    val userId: Int?,
    val categoryName: String,
    val categoryType: String,
    val categoryIcon: Int,
    val categoryColor: Int
) : Serializable

data class CategoryFull(
    val category: CategoryEntity,
    val user: UserEntity?
)

// Room entity with relations
data class CategoryWithUser(
    @Embedded val category: CategoryEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val user: UserEntity?
)

//convert CategoryWithUser to CategoryFull
fun CategoryWithUser.toCategoryFull(): CategoryFull {
    return CategoryFull(
        category = category,
        user = user
    )
}