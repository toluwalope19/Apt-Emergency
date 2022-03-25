package com.example.aptemergency.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aptemergency.data.model.Request
import com.example.aptemergency.repository.EmergencyRepository
import com.example.aptemergency.utils.ApiError
import com.example.aptemergency.utils.LocationLiveData
import com.example.aptemergency.utils.Resource
import com.example.aptemergency.utils.UIEvent
import com.example.aptemergency.utils.Utils.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: EmergencyRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val _sendEmergency = MutableLiveData<UIEvent<Resource<String>>>()
    val sendEmergency = _sendEmergency.asLiveData()

    private val locationLiveData = LocationLiveData(context)

    fun getLocation(): LocationLiveData = locationLiveData

    fun sendEmergency(request: Request) {

        viewModelScope.launch {
            _sendEmergency.postValue(UIEvent(Resource.loading(null)))
            try {
                val response = repository.sendEmergency(request)
                response.message?.let {
                    _sendEmergency.postValue(UIEvent(Resource.success(it)))
                }
            } catch (e: Throwable) {
                // val errorMessage = apiError.extractErrorMessage(e)
                _sendEmergency.postValue(UIEvent(Resource.error(e)))
            }
        }
    }

}