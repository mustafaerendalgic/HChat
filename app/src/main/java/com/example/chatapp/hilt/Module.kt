package com.example.chatapp.hilt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.room.Room
import com.example.chatapp.bluetooth.data.entity.ObjectConstants
import com.example.chatapp.bluetooth.data.repo.BluetoothRepo
import com.example.chatapp.bluetooth.data.repo.client.ClientHandlerImp
import com.example.chatapp.bluetooth.data.repo.general.GeneralHandlerImp
import com.example.chatapp.bluetooth.room.BluetoothDao
import com.example.chatapp.bluetooth.room.RoomDatabaseForBluetooth
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

    @Provides
    @Singleton
    fun getDAO(database: RoomDatabaseForBluetooth): BluetoothDao{
        return database.bluetoothMessageDao()
    }

    @Provides
    @Singleton
    fun getRoomDatabase(@ApplicationContext context: Context): RoomDatabaseForBluetooth{
        return Room.databaseBuilder(context, RoomDatabaseForBluetooth::class.java, ObjectConstants.DATABASE_NAME).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun getRepo(dao: BluetoothDao, adapter: BluetoothAdapter): BluetoothRepo{
        return BluetoothRepo(dao, adapter)
    }

    @Provides
    @Singleton
    fun getClientHandler(repo: BluetoothRepo): ClientHandlerImp{
        return ClientHandlerImp(repo)
    }

    @Provides
    @Singleton
    fun getGeneralHandler(repo: BluetoothRepo): GeneralHandlerImp{
        return GeneralHandlerImp(repo)
    }

}