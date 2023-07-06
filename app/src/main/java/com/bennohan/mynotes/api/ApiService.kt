package com.bennohan.mynotes.api

import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    //AUTH
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("email_or_phone") emailOrPhone: String?,
        @Field("password") password: String?
    ): String

    @FormUrlEncoded
    @POST("auth/register")
    suspend fun register(
        @Field("name") name: String?,
        @Field("phone") phone: String?,
        @Field("email") email: String?,
        @Field("password") password: String?,
        @Field("password_confirmation") passwordConfirm: String?
    ): String

    @POST("auth/logout")
    suspend fun logout(
    ): String

    //USER
    @FormUrlEncoded
    @POST("user/edit-profile")
    suspend fun editProfile(
        @Field("name") Name: String?,
    ): String

    @Multipart
    @POST("user/edit-profile")
    suspend fun editProfilePhoto(
        @Query("name") Name: String?,
        @Part photo : MultipartBody.Part?
    ): String


    @FormUrlEncoded
    @POST("user/edit-password")
    suspend fun editPassword(
        @Field("password_confirmation") passwordConfirmation: String?,
    ): String


    //NOTE
    @GET("note/{id}")
    suspend fun getNoteId(
        @Path("id") noteId : String?
    ): String

    @DELETE("note/delete/{id}")
    suspend fun deleteNote(
        @Path("id") noteId : String?
    ): String

    @POST("note/favorite/{id}")
    suspend fun favouriteNote(
        @Path("id") noteId : String?
    ): String

    @POST("note/unfavorite/{id}")
    suspend fun unFavouriteNote(
        @Path("id") noteId : String?
    ): String

    @FormUrlEncoded
    @POST("note/create")
    suspend fun createNote(
        @Field("title") title: String?,
        @Field("content") content: String?,
        @Field("categories_id") categoriesId: String?,
    ): String

    @Multipart
    @POST("note/create")
    suspend fun createNotePhoto(
        @Query("title") title: String?,
        @Query("content") content: String?,
        @Query("categories_id") categoriesId: String?,
        @Part photo : MultipartBody.Part?
    ): String

    @FormUrlEncoded
    @POST("note/edit/{id}")
    suspend fun editNote(
        @Path("id") noteId : String?,
        @Field("title") title: String?,
        @Field("content") content: String?,
        @Field("categories_id") categoriesId: String?,
    ): String

    @Multipart
    @POST("note/edit/{id}")
    suspend fun editNotePhoto(
        @Path("id") noteId : String?,
        @Query("title") title: String?,
        @Query("content") content: String?,
        @Query("categories_id") categoriesId: String?,
        @Part photo : MultipartBody.Part?
    ): String

    @GET("note/index")
    suspend fun getNote(): String


    //CATEGORIES
    @FormUrlEncoded
    @POST("category/create")
    suspend fun createCategories(
        @Field("category") category: String?,
    ): String

 @FormUrlEncoded
    @POST("category/edit/{id}")
    suspend fun editCategories(
     @Path("id") categoriesId : String?,
     @Field("category") category: String?,
    ): String

    @DELETE("category/delete/{id}")
    suspend fun deleteCategory(
        @Path("id") categoriesId: String?
    ): String

    @GET("category/index")
    suspend fun categoryList(): String

    @GET("category/{categories_id}")
    suspend fun categoryId(
        @Path("categories_id") categoriesId: String?
    ): String




}