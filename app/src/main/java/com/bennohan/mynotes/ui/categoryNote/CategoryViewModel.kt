package com.bennohan.mynotes.ui.categoryNote

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bennohan.mynotes.api.ApiService
import com.bennohan.mynotes.base.BaseViewModel
import com.bennohan.mynotes.database.Categories
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
class CategoryViewModel  @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
    private val userDao: UserDao
) : BaseViewModel() {

    private var _listCategory = MutableSharedFlow<List<Categories?>>()
    var listCategory = _listCategory.asSharedFlow()


    fun getCategory(
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver(
            { apiService.categoryList() },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONArray(ApiCode.DATA).toList<Categories>(gson)
                    Log.d("cek list category",data.toString())
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
    
}