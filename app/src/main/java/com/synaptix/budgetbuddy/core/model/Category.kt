package com.synaptix.budgetbuddy.core.model

import androidx.room.Embedded
import androidx.room.Relation
import com.synaptix.budgetbuddy.data.entity.CategoryEntity
import com.synaptix.budgetbuddy.data.entity.UserEntity
import java.io.Serializable

data class CategoryIn (
    val categoryId: Int = 0,
    val userId: Int?,
    val categoryName: String,
    val categoryType: String,
    val categoryIcon: Int,
    val categoryColor: Int
) : Serializable

data class Category(
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

//convert CategoryWithUser to Category
fun CategoryWithUser.toCategory(): Category {
    return Category(
        category = category,
        user = user
    )
}