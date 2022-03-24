package com.example.aptemergency.di

import android.content.Context
import com.example.aptemergency.data.api.ApiService
import com.example.aptemergency.data.api.RemoteImpl
import com.example.aptemergency.data.api.RemoteSource
import com.example.aptemergency.repository.EmergencyRepository
import com.example.aptemergency.utils.ApiError
import com.example.top_up_weather.utils.interceptors.NetworkConnectivityInterceptor
import com.example.top_up_weather.utils.interceptors.NetworkResponseInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.otaliastudios.cameraview.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {


    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context) = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(NetworkConnectivityInterceptor(context))
            .addInterceptor(NetworkResponseInterceptor())
            .build()
    } else OkHttpClient
        .Builder()
        .build()


    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://dummy.restapiexample.com/api/v1/create/")
            .client(okHttpClient)
            .build()


    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit) = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideApiHelper(apiHelper: RemoteImpl): RemoteSource = apiHelper

    @Provides
    @Singleton
    fun provideApiErrorClass(gson: Gson, @ApplicationContext context: Context): ApiError {
        return ApiError(gson, context)
    }


    @Provides
    @Singleton
    fun provideGSon(): Gson = GsonBuilder().create()


    @Singleton
    @Provides
    fun provideRepository(
        remoteSource: RemoteSource
    ): EmergencyRepository {
        return EmergencyRepository(remoteSource)
    }
}
