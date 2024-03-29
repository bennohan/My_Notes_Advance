package com.bennohan.mynotes.ui.addNote

import androidx.lifecycle.viewModelScope
import com.bennohan.mynotes.api.ApiService
import com.bennohan.mynotes.base.BaseViewModel
import com.bennohan.mynotes.database.categories.Categories
import com.bennohan.mynotes.database.note.Note
import com.crocodic.core.api.ApiCode
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import com.crocodic.core.extension.toList
import com.crocodic.core.extension.toObject
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
) : BaseViewModel() {

    private var _listCategory = MutableSharedFlow<List<Categories?>>()
    var listCategory = _listCategory.asSharedFlow()

    private var _dataNote = MutableSharedFlow<Note?>()
    var dataNote = _dataNote.asSharedFlow()


    fun createNote(
        title: String?,
        content: String?,
        categoriesId: String?,
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.createNote(title, content, categoriesId) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONObject(ApiCode.DATA).toObject<Note>(gson)
                    _dataNote.emit(data)
                    _apiResponse.emit(ApiResponse().responseSuccess("Note Created"))

                }

                override suspend fun onError(response: ApiResponse) {
                    //Ask why the response wont show if the super is gone
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }

    fun createNotePhoto(
        name: String?,
        content: String?,
        categoriesId: String?,
        photo: File
    ) = viewModelScope.launch {
        val fileBody = photo.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("photo", photo.name, fileBody)
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.createNotePhoto(name, content, categoriesId, filePart) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONObject(ApiCode.DATA).toObject<Note>(gson)
                    _dataNote.emit(data)
                    _apiResponse.emit(ApiResponse().responseSuccess("Note Created"))

                }

                override suspend fun onError(response: ApiResponse) {
                    //Ask why the response wont show if the super is gone
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }


    fun getCategory(
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.categoryList() },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONArray(ApiCode.DATA).toList<Categories>(gson)

                    _listCategory.emit(data)
                    _apiResponse.emit(ApiResponse().responseSuccess())
                }

                override suspend fun onError(response: ApiResponse) {
                    //Ask why the response wont show if the super is gone
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }


    fun getNote(
        noteId: String
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.getNoteId(noteId) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONObject(ApiCode.DATA).toObject<Note>(gson)
                    _apiResponse.emit(ApiResponse().responseSuccess())
                    _dataNote.emit(data)
                }

                override suspend fun onError(response: ApiResponse) {
                    //Ask why the response wont show if the super is gone
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }

    fun favouriteNote(
        noteId: String?
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.favouriteNote(noteId) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    _apiResponse.emit(ApiResponse().responseSuccess("Favourite"))
                }

                override suspend fun onError(response: ApiResponse) {
                    //Ask why the response wont show if the super is gone
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }

    fun unFavouriteNote(
        noteId: String?
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.unFavouriteNote(noteId) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    _apiResponse.emit(ApiResponse().responseSuccess("UnFavourite"))
                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }

    fun editNote(
        noteId: String?,
        title: String?,
        content: String?,
        categoriesId: String?,
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.editNote(noteId, title, content, categoriesId) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    _apiResponse.emit(ApiResponse().responseSuccess("Note Edited"))

                }

                override suspend fun onError(response: ApiResponse) {
                    //Ask why the response wont show if the super is gone
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }

    fun editNotePhoto(
        noteId: String?,
        title: String?,
        content: String?,
        categoriesId: String?,
        photo: File
    ) = viewModelScope.launch {
        val fileBody = photo.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("photo", photo.name, fileBody)
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.editNotePhoto(noteId, title, content, categoriesId, filePart) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONObject(ApiCode.DATA).toObject<Note>(gson)
                    _dataNote.emit(data)
                    _apiResponse.emit(ApiResponse().responseSuccess("Note Edited"))

                }

                override suspend fun onError(response: ApiResponse) {
                    //Ask why the response wont show if the super is gone
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }


    fun deleteNote(
        noteId: String?
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.deleteNote(noteId) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    _apiResponse.emit(ApiResponse().responseSuccess("Note Deleted"))
                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }

}