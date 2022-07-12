package com.repo01.repoapp.di

import com.repo01.repoapp.BuildConfig
import com.repo01.repoapp.data.network.LoginRepository
import com.repo01.repoapp.data.network.LoginService
import com.repo01.repoapp.data.network.TokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    companion object {
        const val LOGIN = "login"
        const val INFO = "info"
    }

    @Provides
    fun provideInformationBaseUrl() = BuildConfig.INFORMATION_BASE_URL

    @Provides
    fun provideLoginBaseUrl() = BuildConfig.LOGIN_BASE_URL

    @Singleton
    @Provides
    @Named(LOGIN)
    fun provideLoginOkHttpClient() = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    } else {
        OkHttpClient.Builder().build()
    }

    @Singleton
    @Provides
    fun provideTokenInterceptor() = TokenInterceptor()

    @Singleton
    @Provides
    @Named(INFO)
    fun provideInfoOkHttpClient(tokenInterceptor: TokenInterceptor) = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(tokenInterceptor)
            .build()
    } else {
        OkHttpClient.Builder().build()
    }

    @Singleton
    @Provides
    @Named(INFO)
    fun provideInformationRetrofit(@Named(INFO) okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(provideInformationBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    @Named(LOGIN)
    fun provideLoginRetrofit(@Named(LOGIN) okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(provideLoginBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideLoginService(@Named(LOGIN) retrofit: Retrofit): LoginService {
        return retrofit.create(LoginService::class.java)
    }

    @Singleton
    @Provides
    fun provideLoginRepository(loginService: LoginService) = LoginRepository(loginService)


}