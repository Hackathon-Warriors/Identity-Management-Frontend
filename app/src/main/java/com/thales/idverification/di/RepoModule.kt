package com.thales.idverification.di

import com.thales.idverification.network.INetworkHelper
import com.thales.idverification.network.retrofit.ApiService
import com.thales.idverification.repository.WOTRRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import retrofit2.Retrofit

@Module
@InstallIn(ViewModelComponent::class)
object RepoModule {
    @Provides
    @ViewModelScoped
    fun provideWOTRRepository(networkHelper: INetworkHelper): WOTRRepository {
        return WOTRRepository(networkHelper)
    }

    @Provides
    @ViewModelScoped
    fun provideRetrofitAPIService(retrofit: Retrofit) : ApiService {
        return retrofit.create(ApiService::class.java)
    }
}