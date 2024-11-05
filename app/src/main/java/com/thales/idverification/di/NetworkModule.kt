package com.thales.idverification.di

import com.thales.idverification.network.INetworkHelper
import com.thales.idverification.network.retrofit.RetrofitNetworkHelperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class NetworkModule {
    @Binds
    @ViewModelScoped
    abstract fun provideNetworkHelper(apiService: RetrofitNetworkHelperImpl): INetworkHelper
}