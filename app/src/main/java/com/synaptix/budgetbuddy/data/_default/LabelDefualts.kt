//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.data._default

import com.synaptix.budgetbuddy.data.entity.LabelEntity

// Object holding the default labels for user transactions or budgeting
// These labels are global and accessible by all users, as they have a null User ID
object LabelDefualts {
    // List of default labels to be added to the database
    val defaultLabels = listOf(
        // Label for Needs category
        LabelEntity(0, null, "Needs"),

        // Label for Wants category
        LabelEntity(0, null, "Wants")
    )
}
