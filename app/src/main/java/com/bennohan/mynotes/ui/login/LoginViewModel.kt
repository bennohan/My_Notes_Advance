package com.bennohan.mynotes.ui.login

import androidx.lifecycle.viewModelScope
import com.bennohan.mynotes.api.ApiService
import com.bennohan.mynotes.base.BaseViewModel
import com.bennohan.mynotes.database.Const
import com.bennohan.mynotes.database.User
import com.bennohan.mynotes.database.UserDao
import com.crocodic.core.api.ApiCode
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.data.CoreSession
import com.crocodic.core.extension.toObject
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
    private val session: CoreSession,
    private val userDao: UserDao
) : BaseViewModel() {

    fun login(
        emailOrPhone: String,
        password: String,
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.login(emailOrPhone, password) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONObject(ApiCode.DATA).toObject<User>(gson)
                    val token = response.getJSONObject("data").getJSONObject("token")
                        .getString("access_token")
                    session.setValue(Const.TOKEN.ACCESS_TOKEN, token)
                    session.setValue(Const.LOGIN.EMAIL_PHONE,emailOrPhone)
                    session.setValue(Const.LOGIN.PASSWORD,password)
                    userDao.insert(data.copy(idRoom = 1))
                    _apiResponse.emit(ApiResponse().responseSuccess())
                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }


}