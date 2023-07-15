package com.bennohan.mynotes.database

import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity
data class Note(
    @Expose
    @SerializedName("created_at")
    val createdAt: String?,
    @Expose
    @SerializedName("categories_id")
    val categoriesId: String?,
    @Expose
    @SerializedName("categories_name")
    val categoriesName: String?,
    @Expose
    @SerializedName("content")
    val content: String,
    @Expose
    @SerializedName("created_by")
    val createdBy: String,
    @Expose
    @SerializedName("favorite")
    val favorite: Boolean,
    @Expose
    @SerializedName("id")
    val id: String,
    @Expose
    @SerializedName("photo")
    val photo: String,
    @Expose
    @SerializedName("title")
    val title: String,
    @Expose
    @SerializedName("updated_at")
    val updatedAt: String,
    @Expose
    @SerializedName("updated_at_formatted")
    val updatedAtFormatted: String,
    @Expose
    @SerializedName("created_at_formatted")
    val createdAtFormatted: String
)