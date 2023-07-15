package com.bennohan.mynotes.ui.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bennohan.mynotes.api.ApiService
import com.bennohan.mynotes.base.BaseViewModel
import com.bennohan.mynotes.database.Categories
import com.bennohan.mynotes.database.Note
import com.bennohan.mynotes.database.UserDao
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
import org.json.JSONObject
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
    private val userDao: UserDao
) : BaseViewModel() {

    private var _listNote = MutableSharedFlow<List<Note?>>()
    var listNote = _listNote.asSharedFlow()


    fun getNote(
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.getNote() },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONArray(ApiCode.DATA).toList<Note>(gson)
                    Log.d("cek list",data.toString())
                    _listNote.emit(data)
                    _apiResponse.emit(ApiResponse().responseSuccess())
                }

                override suspend fun onError(response: ApiResponse) {
                    //Ask why the response wont show if the super is gone
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }

    // logout
    fun logout() = viewModelScope.launch {
        ApiObserver({ apiService.logout() },
            false, object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    _apiResponse.emit(ApiResponse().responseSuccess())
                    userDao.deleteAll()
                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }


}