package com.example.chatapp.hilt

import com.example.chatapp.data.repo.Repo
import com.example.chatapp.retrofit.DAO
import com.example.chatapp.retrofit.DAOAccessFunction
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun getDao(): DAO{
        return DAOAccessFunction.getTheDao()
    }

    @Provides
    @Singleton
    fun getRepo(): Repo{
        return Repo(getDao())
    }

}