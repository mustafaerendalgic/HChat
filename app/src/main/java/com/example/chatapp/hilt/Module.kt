package com.example.chatapp.hilt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.getSystemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule{

    @Provides
    @Singleton
    fun getBluetoothManager(@ApplicationContext context: Context): BluetoothManager{
        return context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    @Provides
    @Singleton
    fun getBluetoothAdapter(bm: BluetoothManager): BluetoothAdapter{
        return bm.adapter
    }

}