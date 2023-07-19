package com.bennohan.mynotes.database.categories

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Categories(
    @Expose
    @SerializedName("category")
    val category: String?,
    @Expose
    @SerializedName("created_by")
    val createdBy: String?,
    @Expose
    @SerializedName("created_at")
    val createdAt: String?,
    @Expose
    @SerializedName("updated_at")
    val updatedAT: String?,
    @Expose
    @SerializedName("id")
    val id: String?
){
    override fun toString(): String {
        return category.toString()
    }

}