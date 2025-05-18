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

package com.synaptix.budgetbuddy.data.entity

import androidx.room.*
import androidx.annotation.NonNull

//
// ================================
// Transaction Entity
// ================================
// Represents a financial transaction made by the user.
// Each transaction is linked to a user, wallet, and category.
// Can optionally have a note, image, and recurrence pattern.
//
@Entity(
    tableName = "transaction_table",
    foreignKeys = [
        // Links transaction to the UserEntity
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        // Links transaction to the WalletEntity
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["wallet_id"],
            childColumns = ["wallet_id"],
            onDelete = ForeignKey.CASCADE
        ),
        // Links transaction to the CategoryEntity
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["wallet_id"]),
        Index(value = ["category_id"]),
        Index(value = ["date"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) 
    @ColumnInfo(name = "transaction_id")
    val transaction_id: Int = 0,

    @ColumnInfo(name = "user_id")
    val user_id: Int,

    @ColumnInfo(name = "wallet_id")
    val wallet_id: Int,

    @ColumnInfo(name = "category_id")
    val category_id: Int,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "note")
    val note: String?,     // Optional note or description for the transaction

    @ColumnInfo(name = "currency")
    val currency: String,  // Currency type (e.g., USD, ZAR)

    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    val image: ByteArray?, // Optional image attachment (e.g., receipt)

    @ColumnInfo(name = "recurrence")
    val recurrence: String? // Optional recurrence pattern (e.g., monthly)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransactionEntity

        if (transaction_id != other.transaction_id) return false
        if (user_id != other.user_id) return false
        if (wallet_id != other.wallet_id) return false
        if (category_id != other.category_id) return false
        if (amount != other.amount) return false
        if (date != other.date) return false
        if (note != other.note) return false
        if (currency != other.currency) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false
        if (recurrence != other.recurrence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transaction_id
        result = 31 * result + user_id
        result = 31 * result + wallet_id
        result = 31 * result + category_id
        result = 31 * result + amount.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + (note?.hashCode() ?: 0)
        result = 31 * result + currency.hashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        result = 31 * result + (recurrence?.hashCode() ?: 0)
        return result
    }
}
