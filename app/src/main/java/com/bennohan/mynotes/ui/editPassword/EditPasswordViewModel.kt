package com.bennohan.mynotes.ui.editPassword

import androidx.lifecycle.viewModelScope
import com.bennohan.mynotes.api.ApiService
import com.bennohan.mynotes.base.BaseViewModel
import com.bennohan.mynotes.database.user.UserDao
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class EditPasswordViewModel @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
    private val userDao: UserDao
) : BaseViewModel() {

    fun editPassword(
        newPassword : String,
        passwordConfirmation: String,
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.editPassword(newPassword, passwordConfirmation) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    _apiResponse.emit(ApiResponse().responseSuccess())
                }

                override suspend fun onError(response: ApiResponse) {
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }

}